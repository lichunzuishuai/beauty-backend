package com.lcs.beautybackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.FavoriteVO;
import com.lcs.beautybackend.service.FavoriteService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏管理接口
 */
@Slf4j
@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏管理", description = "收藏相关接口")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private UserService userService;

    /**
     * 收藏化妆师
     */
    @PostMapping("/artist/{artistId}")
    @Operation(summary = "收藏化妆师", description = "收藏化妆师")
    public BaseResponse<Boolean> favoriteArtist(@PathVariable Long artistId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(artistId == null || artistId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = favoriteService.addFavorite(loginUser.getId(), 1, artistId);
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏化妆师
     */
    @DeleteMapping("/artist/{artistId}")
    @Operation(summary = "取消收藏化妆师", description = "取消收藏化妆师")
    public BaseResponse<Boolean> unfavoriteArtist(@PathVariable Long artistId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(artistId == null || artistId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = favoriteService.removeFavorite(loginUser.getId(), 1, artistId);
        return ResultUtils.success(result);
    }

    /**
     * 收藏服务套餐
     */
    @PostMapping("/service/{serviceId}")
    @Operation(summary = "收藏服务套餐", description = "收藏服务套餐")
    public BaseResponse<Boolean> favoriteService(@PathVariable Long serviceId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(serviceId == null || serviceId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = favoriteService.addFavorite(loginUser.getId(), 2, serviceId);
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏服务套餐
     */
    @DeleteMapping("/service/{serviceId}")
    @Operation(summary = "取消收藏服务套餐", description = "取消收藏服务套餐")
    public BaseResponse<Boolean> unfavoriteService(@PathVariable Long serviceId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(serviceId == null || serviceId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = favoriteService.removeFavorite(loginUser.getId(), 2, serviceId);
        return ResultUtils.success(result);
    }

    /**
     * 查询我的收藏列表
     */
    @GetMapping("/my/list")
    @Operation(summary = "查询我的收藏列表", description = "分页查询当前用户的收藏")
    public BaseResponse<Page<FavoriteVO>> getMyFavorites(
            @Parameter(description = "收藏类型: 1化妆师 2服务套餐") @RequestParam(required = false) Integer targetType,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<FavoriteVO> page = favoriteService.getMyFavorites(loginUser.getId(), targetType, current, pageSize);
        return ResultUtils.success(page);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    @Operation(summary = "检查是否已收藏", description = "检查指定目标是否已被收藏")
    public BaseResponse<Boolean> checkFavorite(
            @Parameter(description = "收藏类型: 1化妆师 2服务套餐", required = true) @RequestParam Integer targetType,
            @Parameter(description = "目标ID", required = true) @RequestParam Long targetId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(targetType == null || targetId == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean favorited = favoriteService.isFavorited(loginUser.getId(), targetType, targetId);
        return ResultUtils.success(favorited);
    }
}
