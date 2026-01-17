package com.lcs.beautybackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.service.ServiceCreateRequest;
import com.lcs.beautybackend.model.dto.service.ServiceUpdateRequest;
import com.lcs.beautybackend.model.entity.*;
import com.lcs.beautybackend.model.vo.ServiceVO;
import com.lcs.beautybackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 化妆师端口接口
 * 仅限化妆师角色访问
 */
@Slf4j
@RestController
@RequestMapping("/artist-portal")
@Tag(name = "化妆师端口", description = "化妆师专属功能接口")
public class ArtistPortalController {

    @Resource
    private UserService userService;

    @Resource
    private ArtistService artistService;

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private ServiceService serviceService;

    @Resource
    private ReviewService reviewService;

    /**
     * 检查是否为化妆师
     */
    private Artist checkArtist(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser.getRole() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅限化妆师访问");
        }
        // 获取化妆师信息
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        Artist artist = artistService.getOne(queryWrapper);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师信息不存在");
        }
        return artist;
    }

    // ==================== 预约管理 ====================

    /**
     * 获取我的预约列表
     */
    @GetMapping("/appointments")
    @Operation(summary = "获取我的预约列表")
    public BaseResponse<Page<Appointment>> getMyAppointments(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);

        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("artistId", artist.getId());
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("createTime");

        Page<Appointment> page = appointmentService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 确认预约
     */
    @PostMapping("/appointment/confirm/{id}")
    @Operation(summary = "确认预约")
    public BaseResponse<Boolean> confirmAppointment(@PathVariable Long id, HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Appointment appointment = appointmentService.getById(id);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!appointment.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        ThrowUtils.throwIf(appointment.getStatus() != 1, ErrorCode.OPERATION_ERROR, "只能确认待确认状态的预约");

        appointment.setStatus(2); // 已确认
        appointment.setUpdateTime(new Date());
        boolean result = appointmentService.updateById(appointment);
        return ResultUtils.success(result);
    }

    /**
     * 拒绝预约
     */
    @PostMapping("/appointment/reject/{id}")
    @Operation(summary = "拒绝预约")
    public BaseResponse<Boolean> rejectAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Appointment appointment = appointmentService.getById(id);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!appointment.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        ThrowUtils.throwIf(appointment.getStatus() != 1, ErrorCode.OPERATION_ERROR, "只能拒绝待确认状态的预约");

        String rejectReason = body != null ? body.get("reason") : null;
        appointment.setStatus(3); // 已拒绝
        appointment.setRemark(rejectReason);
        appointment.setUpdateTime(new Date());
        boolean result = appointmentService.updateById(appointment);
        return ResultUtils.success(result);
    }

    // ==================== 工作状态 ====================

    /**
     * 获取当前工作状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取工作状态")
    public BaseResponse<Integer> getWorkStatus(HttpServletRequest request) {
        Artist artist = checkArtist(request);
        return ResultUtils.success(artist.getStatus());
    }

    /**
     * 设置工作状态
     */
    @PutMapping("/status")
    @Operation(summary = "设置工作状态")
    public BaseResponse<Boolean> setWorkStatus(
            @RequestBody java.util.Map<String, Integer> body,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        Integer status = body.get("status");
        ThrowUtils.throwIf(status == null || (status != 0 && status != 1), ErrorCode.PARAMS_ERROR, "状态值无效");

        artist.setStatus(status);
        artist.setUpdateTime(new Date());
        boolean result = artistService.updateById(artist);
        return ResultUtils.success(result);
    }

    // ==================== 服务套餐管理 ====================

    /**
     * 获取我的服务套餐列表
     */
    @GetMapping("/services")
    @Operation(summary = "获取我的服务套餐")
    public BaseResponse<List<ServiceVO>> getMyServices(HttpServletRequest request) {
        Artist artist = checkArtist(request);

        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("artistId", artist.getId());
        queryWrapper.eq("isDeleted", 0);
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        List<Service> serviceList = serviceService.list(queryWrapper);
        List<ServiceVO> voList = serviceList.stream()
                .map(serviceService::toServiceVO)
                .collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 添加服务套餐
     */
    @PostMapping("/service")
    @Operation(summary = "添加服务套餐")
    public BaseResponse<Long> createService(@RequestBody ServiceCreateRequest createRequest,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(createRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(createRequest.getName()), ErrorCode.PARAMS_ERROR, "套餐名称不能为空");
        ThrowUtils.throwIf(createRequest.getPrice() == null, ErrorCode.PARAMS_ERROR, "价格不能为空");

        Service service = new Service();
        service.setArtistId(artist.getId());
        service.setName(createRequest.getName());
        service.setDescription(createRequest.getDescription());
        service.setPrice(createRequest.getPrice());
        service.setDuration(createRequest.getDuration());
        service.setCoverImage(createRequest.getCoverImage());
        service.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);
        service.setStatus(1); // 默认上架
        service.setCreateTime(new Date());
        service.setUpdateTime(new Date());

        boolean saved = serviceService.save(service);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "添加失败");

        return ResultUtils.success(service.getId());
    }

    /**
     * 更新服务套餐
     */
    @PutMapping("/service/{id}")
    @Operation(summary = "更新服务套餐")
    public BaseResponse<Boolean> updateService(
            @PathVariable Long id,
            @RequestBody ServiceUpdateRequest updateRequest,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Service service = serviceService.getById(id);
        ThrowUtils.throwIf(service == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!service.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作此服务");

        if (StringUtils.isNotBlank(updateRequest.getName())) {
            service.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            service.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            service.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getDuration() != null) {
            service.setDuration(updateRequest.getDuration());
        }
        if (updateRequest.getCoverImage() != null) {
            service.setCoverImage(updateRequest.getCoverImage());
        }
        if (updateRequest.getSortOrder() != null) {
            service.setSortOrder(updateRequest.getSortOrder());
        }
        service.setUpdateTime(new Date());

        boolean result = serviceService.updateById(service);
        return ResultUtils.success(result);
    }

    /**
     * 更新服务状态（上下架）
     */
    @PutMapping("/service/status/{id}")
    @Operation(summary = "上下架服务")
    public BaseResponse<Boolean> updateServiceStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> body,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Service service = serviceService.getById(id);
        ThrowUtils.throwIf(service == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!service.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作此服务");

        Integer status = body.get("status");
        ThrowUtils.throwIf(status == null || (status != 0 && status != 1), ErrorCode.PARAMS_ERROR, "状态值无效");

        service.setStatus(status);
        service.setUpdateTime(new Date());
        boolean result = serviceService.updateById(service);
        return ResultUtils.success(result);
    }

    /**
     * 删除服务套餐
     */
    @DeleteMapping("/service/{id}")
    @Operation(summary = "删除服务套餐")
    public BaseResponse<Boolean> deleteService(@PathVariable Long id, HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Service service = serviceService.getById(id);
        ThrowUtils.throwIf(service == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!service.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权操作此服务");

        service.setIsDeleted(1);
        service.setUpdateTime(new Date());
        boolean result = serviceService.updateById(service);
        return ResultUtils.success(result);
    }

    // ==================== 评价管理 ====================

    /**
     * 获取我的服务评价列表
     */
    @GetMapping("/reviews")
    @Operation(summary = "获取客户评价")
    public BaseResponse<Page<Review>> getMyReviews(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);

        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("artistId", artist.getId());
        queryWrapper.eq("isDeleted", 0);
        queryWrapper.orderByDesc("createTime");

        Page<Review> page = reviewService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    /**
     * 回复评价
     */
    @PostMapping("/review/reply/{id}")
    @Operation(summary = "回复评价")
    public BaseResponse<Boolean> replyReview(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest request) {
        Artist artist = checkArtist(request);
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        Review review = reviewService.getById(id);
        ThrowUtils.throwIf(review == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!review.getArtistId().equals(artist.getId()), ErrorCode.NO_AUTH_ERROR, "无权回复此评价");

        String reply = body.get("reply");
        ThrowUtils.throwIf(StringUtils.isBlank(reply), ErrorCode.PARAMS_ERROR, "回复内容不能为空");

        review.setReply(reply);
        review.setReplyTime(new Date());
        review.setUpdateTime(new Date());
        boolean result = reviewService.updateById(review);
        return ResultUtils.success(result);
    }

    /**
     * 获取化妆师个人信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public BaseResponse<Artist> getProfile(HttpServletRequest request) {
        Artist artist = checkArtist(request);
        return ResultUtils.success(artist);
    }
}
