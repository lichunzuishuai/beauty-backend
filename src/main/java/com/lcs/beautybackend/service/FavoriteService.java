package com.lcs.beautybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.model.entity.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.FavoriteVO;

/**
 * @author lcs
 * @description 针对表【favorite(收藏表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     *
     * @param userId     用户ID
     * @param targetType 收藏类型 1化妆师 2服务套餐
     * @param targetId   目标ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 取消收藏
     *
     * @param userId     用户ID
     * @param targetType 收藏类型
     * @param targetId   目标ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Integer targetType, Long targetId);

    /**
     * 查询我的收藏列表
     *
     * @param userId     用户ID
     * @param targetType 收藏类型（可选）
     * @param current    当前页
     * @param pageSize   每页数量
     * @return 收藏列表
     */
    Page<FavoriteVO> getMyFavorites(Long userId, Integer targetType, int current, int pageSize);

    /**
     * 检查是否已收藏
     *
     * @param userId     用户ID
     * @param targetType 收藏类型
     * @param targetId   目标ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Integer targetType, Long targetId);
}
