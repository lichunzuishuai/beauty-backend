package com.lcs.beautybackend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.mapper.ReviewMapper;
import com.lcs.beautybackend.model.dto.review.ReviewCreateRequest;
import com.lcs.beautybackend.model.entity.Appointment;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.Review;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ReviewVO;
import com.lcs.beautybackend.service.AppointmentService;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.ReviewService;
import com.lcs.beautybackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lcs
 * @description 针对表【review(评价表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review>
        implements ReviewService {

    @Resource
    @Lazy
    private AppointmentService appointmentService;

    @Resource
    private ArtistService artistService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(Long userId, ReviewCreateRequest request) {
        // 参数校验
        if (request.getAppointmentId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约订单ID不能为空");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
        }

        // 验证订单
        Appointment appointment = appointmentService.getById(request.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约订单不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权评价此订单");
        }
        if (appointment.getStatus() != 4) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已完成的订单才能评价");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Review> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Review::getAppointmentId, request.getAppointmentId());
        if (this.count(existQuery) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该订单已评价");
        }

        // 创建评价
        Review review = new Review();
        review.setAppointmentId(request.getAppointmentId());
        review.setUserId(userId);
        review.setArtistId(appointment.getArtistId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setStatus(1); // 显示

        // JSON 数组处理
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            review.setImages(JSONUtil.toJsonStr(request.getImages()));
        }

        boolean saved = this.save(review);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "评价失败");
        }

        // 更新化妆师平均评分
        updateArtistRating(appointment.getArtistId());

        return review.getId();
    }

    @Override
    public Page<ReviewVO> getArtistReviews(Long artistId, int current, int pageSize) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getArtistId, artistId)
                .eq(Review::getStatus, 1) // 只查询显示的评价
                .orderByDesc(Review::getCreateTime);

        Page<Review> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<ReviewVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ReviewVO> voList = page.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setUserId(review.getUserId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setReply(review.getReply());
            vo.setReplyTime(review.getReplyTime());
            vo.setCreateTime(review.getCreateTime());

            // 获取用户信息
            User user = userService.getById(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            // 解析图片JSON
            if (review.getImages() != null) {
                vo.setImages(JSONUtil.toList(review.getImages(), String.class));
            }

            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 更新化妆师平均评分
     */
    private void updateArtistRating(Long artistId) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getArtistId, artistId)
                .eq(Review::getStatus, 1);

        List<Review> reviews = this.list(queryWrapper);
        if (reviews.isEmpty()) {
            return;
        }

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(5.0);

        Artist artist = artistService.getById(artistId);
        if (artist != null) {
            artist.setRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
            artistService.updateById(artist);
        }
    }
}
