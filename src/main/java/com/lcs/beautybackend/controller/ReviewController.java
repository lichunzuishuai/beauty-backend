package com.lcs.beautybackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.review.ReviewCreateRequest;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ReviewVO;
import com.lcs.beautybackend.service.ReviewService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价管理接口
 */
@Slf4j
@RestController
@RequestMapping("/review")
@Tag(name = "评价管理", description = "评价相关接口")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @Resource
    private UserService userService;

    /**
     * 提交评价
     */
    @PostMapping("/create")
    @Operation(summary = "提交评价", description = "对已完成的预约订单提交评价")
    public BaseResponse<Long> createReview(@RequestBody ReviewCreateRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long reviewId = reviewService.createReview(loginUser.getId(), request);
        return ResultUtils.success(reviewId);
    }

    /**
     * 获取化妆师评价列表
     */
    @GetMapping("/artist/{artistId}")
    @Operation(summary = "获取化妆师评价列表", description = "分页查询化妆师的评价")
    public BaseResponse<Page<ReviewVO>> getArtistReviews(
            @PathVariable Long artistId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        ThrowUtils.throwIf(artistId == null || artistId <= 0, ErrorCode.PARAMS_ERROR);
        Page<ReviewVO> page = reviewService.getArtistReviews(artistId, current, pageSize);
        return ResultUtils.success(page);
    }
}
