package com.lcs.beautybackend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.model.dto.artistapplication.ArtistApplyRequest;
import com.lcs.beautybackend.model.entity.Artistapplication;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ArtistApplicationVO;
import com.lcs.beautybackend.service.ArtistapplicationService;
import com.lcs.beautybackend.mapper.ArtistapplicationMapper;
import com.lcs.beautybackend.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author lcs
 * @description 针对表【artistapplication(化妆师入驻申请表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@Service
public class ArtistapplicationServiceImpl extends ServiceImpl<ArtistapplicationMapper, Artistapplication>
        implements ArtistapplicationService {

    @Resource
    private UserService userService;

    @Override
    public Long apply(Long userId, ArtistApplyRequest request) {
        // 参数校验
        if (StringUtils.isBlank(request.getRealName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "真实姓名不能为空");
        }
        if (StringUtils.isBlank(request.getPhone())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "联系电话不能为空");
        }

        // 检查用户是否已经是化妆师
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (user.getRole() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经是化妆师");
        }

        // 检查是否有待审核的申请
        LambdaQueryWrapper<Artistapplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Artistapplication::getUserId, userId)
                .eq(Artistapplication::getStatus, 0);
        Artistapplication existingApplication = this.getOne(queryWrapper);
        if (existingApplication != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已有待审核的申请，请勿重复提交");
        }

        // 创建申请
        Artistapplication application = new Artistapplication();
        application.setUserId(userId);
        application.setRealName(request.getRealName());
        application.setIdCard(request.getIdCard());
        application.setPhone(request.getPhone());
        application.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0);
        application.setSpecialties(request.getSpecialties());
        application.setIntroduction(request.getIntroduction());
        application.setStatus(0); // 待审核

        // JSON 数组处理
        if (request.getCertificateImages() != null && !request.getCertificateImages().isEmpty()) {
            application.setCertificateImages(JSONUtil.toJsonStr(request.getCertificateImages()));
        }
        if (request.getPortfolioImages() != null && !request.getPortfolioImages().isEmpty()) {
            application.setPortfolioImages(JSONUtil.toJsonStr(request.getPortfolioImages()));
        }

        boolean saved = this.save(application);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交申请失败");
        }

        return application.getId();
    }

    @Override
    public ArtistApplicationVO getMyApplicationStatus(Long userId) {
        // 查询最新的申请记录
        LambdaQueryWrapper<Artistapplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Artistapplication::getUserId, userId)
                .orderByDesc(Artistapplication::getCreateTime)
                .last("LIMIT 1");
        Artistapplication application = this.getOne(queryWrapper);

        if (application == null) {
            return null;
        }

        ArtistApplicationVO vo = new ArtistApplicationVO();
        vo.setId(application.getId());
        vo.setStatus(application.getStatus());
        vo.setRejectReason(application.getRejectReason());
        vo.setCreateTime(application.getCreateTime());
        vo.setReviewedAt(application.getReviewedAt());

        return vo;
    }
}
