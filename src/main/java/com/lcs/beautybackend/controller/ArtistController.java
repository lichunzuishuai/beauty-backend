package com.lcs.beautybackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.artist.ArtistQueryRequest;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.Service;
import com.lcs.beautybackend.model.vo.ArtistVO;
import com.lcs.beautybackend.model.vo.ServiceVO;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 化妆师接口
 */
@Slf4j
@RestController
@RequestMapping("/artist")
@Tag(name = "化妆师模块", description = "化妆师相关接口")
public class ArtistController {

    @Resource
    private ArtistService artistService;

    @Resource
    private ServiceService serviceService;

    /**
     * 分页获取化妆师列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页获取化妆师列表", description = "支持多条件筛选")
    public BaseResponse<Page<ArtistVO>> listArtistByPage(ArtistQueryRequest artistQueryRequest) {
        if (artistQueryRequest == null) {
            artistQueryRequest = new ArtistQueryRequest();
        }
        int current = artistQueryRequest.getCurrent();
        int pageSize = artistQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "每页数量不能超过50");

        Page<Artist> artistPage = artistService.page(
                new Page<>(current, pageSize),
                artistService.getQueryWrapper(artistQueryRequest));
        return ResultUtils.success(artistService.getArtistVOPage(artistPage));
    }

    /**
     * 根据ID获取化妆师详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取化妆师详情", description = "根据ID获取化妆师详细信息")
    public BaseResponse<ArtistVO> getArtistById(
            @Parameter(description = "化妆师ID") @PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Artist artist = artistService.getById(id);
        ThrowUtils.throwIf(artist == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(artistService.getArtistVO(artist));
    }

    /**
     * 获取推荐化妆师列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "获取推荐化妆师", description = "获取推荐的化妆师列表")
    public BaseResponse<List<ArtistVO>> listRecommendArtist(
            @Parameter(description = "数量限制，默认6") @RequestParam(defaultValue = "6") Integer limit) {
        ThrowUtils.throwIf(limit > 20, ErrorCode.PARAMS_ERROR, "数量不能超过20");
        List<ArtistVO> artistVOList = artistService.listRecommendArtist(limit);
        return ResultUtils.success(artistVOList);
    }

    /**
     * 搜索化妆师
     */
    @GetMapping("/search")
    @Operation(summary = "搜索化妆师", description = "根据关键词搜索化妆师")
    public BaseResponse<Page<ArtistVO>> searchArtist(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "每页数量不能超过50");

        ArtistQueryRequest queryRequest = new ArtistQueryRequest();
        queryRequest.setRealName(keyword);
        queryRequest.setCurrent(current);
        queryRequest.setPageSize(pageSize);
        queryRequest.setStatus(1); // 只搜索可接单的化妆师

        Page<Artist> artistPage = artistService.page(
                new Page<>(current, pageSize),
                artistService.getQueryWrapper(queryRequest));
        return ResultUtils.success(artistService.getArtistVOPage(artistPage));
    }

    /**
     * 获取化妆师的服务套餐列表
     */
    @GetMapping("/{id}/services")
    @Operation(summary = "获取化妆师服务套餐", description = "获取指定化妆师的所有上架服务套餐")
    public BaseResponse<List<ServiceVO>> getArtistServices(
            @Parameter(description = "化妆师ID") @PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询该化妆师的所有上架服务
        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("artistId", id);
        queryWrapper.eq("status", 1); // 只查上架的服务
        queryWrapper.eq("isDeleted", 0);
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        List<Service> serviceList = serviceService.list(queryWrapper);
        List<ServiceVO> serviceVOList = serviceList.stream()
                .map(serviceService::toServiceVO)
                .collect(Collectors.toList());

        return ResultUtils.success(serviceVOList);
    }
}
