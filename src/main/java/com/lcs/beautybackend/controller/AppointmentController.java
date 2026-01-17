package com.lcs.beautybackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.appointment.AppointmentCancelRequest;
import com.lcs.beautybackend.model.dto.appointment.AppointmentCreateRequest;
import com.lcs.beautybackend.model.dto.appointment.AppointmentRescheduleRequest;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.AppointmentDetailVO;
import com.lcs.beautybackend.model.vo.AppointmentVO;
import com.lcs.beautybackend.service.AppointmentService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 预约管理接口
 */
@Slf4j
@RestController
@RequestMapping("/appointment")
@Tag(name = "预约管理", description = "预约订单相关接口")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private UserService userService;

    /**
     * 创建预约
     */
    @PostMapping("/create")
    @Operation(summary = "创建预约", description = "预约化妆师服务")
    public BaseResponse<AppointmentVO> createAppointment(@RequestBody AppointmentCreateRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        AppointmentVO vo = appointmentService.createAppointment(loginUser.getId(), request);
        return ResultUtils.success(vo);
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消预约", description = "取消预约订单（仅限待支付/待确认/已确认状态）")
    public BaseResponse<Boolean> cancelAppointment(@PathVariable Long id,
            @RequestBody(required = false) AppointmentCancelRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        String reason = request != null ? request.getCancelReason() : null;
        boolean result = appointmentService.cancelAppointment(loginUser.getId(), id, reason);
        return ResultUtils.success(result);
    }

    /**
     * 修改预约时间
     */
    @PutMapping("/reschedule/{id}")
    @Operation(summary = "修改预约时间", description = "修改预约时间（仅限待确认/已确认状态）")
    public BaseResponse<Boolean> rescheduleAppointment(@PathVariable Long id,
            @RequestBody AppointmentRescheduleRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = appointmentService.rescheduleAppointment(loginUser.getId(), id, request);
        return ResultUtils.success(result);
    }

    /**
     * 查询我的预约列表
     */
    @GetMapping("/my/list")
    @Operation(summary = "查询我的预约列表", description = "分页查询当前用户的预约订单")
    public BaseResponse<Page<AppointmentVO>> getMyAppointments(
            @Parameter(description = "订单状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<AppointmentVO> page = appointmentService.getMyAppointments(loginUser.getId(), status, current, pageSize);
        return ResultUtils.success(page);
    }

    /**
     * 查询预约详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询预约详情", description = "查询预约订单详情")
    public BaseResponse<AppointmentDetailVO> getAppointmentDetail(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        AppointmentDetailVO vo = appointmentService.getAppointmentDetail(loginUser.getId(), id);
        return ResultUtils.success(vo);
    }
}
