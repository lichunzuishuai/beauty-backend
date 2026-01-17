package com.lcs.beautybackend.controller;

import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.payment.PaymentPayRequest;
import com.lcs.beautybackend.model.dto.payment.PaymentRefundRequest;
import com.lcs.beautybackend.model.entity.Payment;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.PaymentInfoVO;
import com.lcs.beautybackend.model.vo.PaymentResultVO;
import com.lcs.beautybackend.service.PaymentService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付管理接口
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private UserService userService;

    /**
     * 获取支付信息
     */
    @GetMapping("/info/{appointmentId}")
    @Operation(summary = "获取支付信息", description = "根据预约订单ID获取支付信息")
    public BaseResponse<PaymentInfoVO> getPaymentInfo(@PathVariable Long appointmentId,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(appointmentId == null || appointmentId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        PaymentInfoVO vo = paymentService.getPaymentInfo(loginUser.getId(), appointmentId);
        return ResultUtils.success(vo);
    }

    /**
     * 模拟支付
     */
    @PostMapping("/pay")
    @Operation(summary = "模拟支付", description = "模拟支付（仅用于测试）")
    public BaseResponse<PaymentResultVO> pay(@RequestBody PaymentPayRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        PaymentResultVO result = paymentService.pay(loginUser.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{paymentNo}")
    @Operation(summary = "查询支付状态", description = "根据支付流水号查询支付状态")
    public BaseResponse<Payment> getPaymentStatus(@PathVariable String paymentNo,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(paymentNo == null, ErrorCode.PARAMS_ERROR);
        userService.getLoginUser(httpRequest); // 验证登录
        Payment payment = paymentService.getPaymentStatus(paymentNo);
        ThrowUtils.throwIf(payment == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(payment);
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund")
    @Operation(summary = "申请退款", description = "申请退款（仅限待确认/已拒绝状态）")
    public BaseResponse<Boolean> refund(@RequestBody PaymentRefundRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = paymentService.refund(loginUser.getId(), request);
        return ResultUtils.success(result);
    }
}
