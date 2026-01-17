package com.lcs.beautybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.model.dto.review.ReviewCreateRequest;
import com.lcs.beautybackend.model.entity.Review;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.ReviewVO;

/**
 * @author lcs
 * @description 针对表【review(评价表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface ReviewService extends IService<Review> {

    /**
     * 创建评价
     *
     * @param userId  用户ID
     * @param request 评价信息
     * @return 评价ID
     */
    Long createReview(Long userId, ReviewCreateRequest request);

    /**
     * 获取化妆师评价列表
     *
     * @param artistId 化妆师ID
     * @param current  当前页
     * @param pageSize 每页数量
     * @return 评价列表
     */
    Page<ReviewVO> getArtistReviews(Long artistId, int current, int pageSize);
}
