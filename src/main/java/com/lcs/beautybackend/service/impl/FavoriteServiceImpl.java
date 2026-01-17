package com.lcs.beautybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.mapper.FavoriteMapper;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.Favorite;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.FavoriteVO;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.FavoriteService;
import com.lcs.beautybackend.service.ServiceService;
import com.lcs.beautybackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lcs
 * @description 针对表【favorite(收藏表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
        implements FavoriteService {

    @Resource
    private ArtistService artistService;

    @Resource
    private ServiceService serviceService;

    @Resource
    private UserService userService;

    @Override
    public boolean addFavorite(Long userId, Integer targetType, Long targetId) {
        // 验证类型
        if (targetType != 1 && targetType != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "收藏类型错误");
        }

        // 验证目标存在
        if (targetType == 1) {
            Artist artist = artistService.getById(targetId);
            if (artist == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
            }
        } else {
            com.lcs.beautybackend.model.entity.Service service = serviceService.getById(targetId);
            if (service == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
            }
        }

        // 检查是否已收藏
        if (isFavorited(userId, targetType, targetId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已收藏，请勿重复操作");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);

        return this.save(favorite);
    }

    @Override
    public boolean removeFavorite(Long userId, Integer targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        return this.remove(queryWrapper);
    }

    @Override
    public Page<FavoriteVO> getMyFavorites(Long userId, Integer targetType, int current, int pageSize) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId);
        if (targetType != null) {
            queryWrapper.eq(Favorite::getTargetType, targetType);
        }
        queryWrapper.orderByDesc(Favorite::getCreateTime);

        Page<Favorite> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<FavoriteVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<FavoriteVO> voList = page.getRecords().stream().map(favorite -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(favorite.getId());
            vo.setTargetType(favorite.getTargetType());
            vo.setTargetId(favorite.getTargetId());
            vo.setCreateTime(favorite.getCreateTime());

            // 获取名称和图片
            if (favorite.getTargetType() == 1) {
                Artist artist = artistService.getById(favorite.getTargetId());
                if (artist != null) {
                    vo.setTargetName(artist.getRealName());
                    // 获取化妆师头像
                    User artistUser = userService.getById(artist.getUserId());
                    if (artistUser != null) {
                        vo.setTargetImage(artistUser.getAvatar());
                    }
                }
            } else {
                com.lcs.beautybackend.model.entity.Service service = serviceService.getById(favorite.getTargetId());
                if (service != null) {
                    vo.setTargetName(service.getName());
                    vo.setTargetImage(service.getCoverImage());
                }
            }

            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public boolean isFavorited(Long userId, Integer targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);

        return this.count(queryWrapper) > 0;
    }
}
