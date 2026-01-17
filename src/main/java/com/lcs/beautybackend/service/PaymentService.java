package com.lcs.beautybackend.service;

import com.lcs.beautybackend.model.dto.payment.PaymentPayRequest;
import com.lcs.beautybackend.model.dto.payment.PaymentRefundRequest;
import com.lcs.beautybackend.model.entity.Payment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.PaymentInfoVO;
import com.lcs.beautybackend.model.vo.PaymentResultVO;

import java.math.BigDecimal;

/**
 * @author lcs
 * @description 针对表【payment(支付记录表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付记录
     *
     * @param appointmentId 预约订单ID
     * @param amount        金额
     * @return 支付记录
     */
    Payment createPayment(Long appointmentId, BigDecimal amount);

    /**
     * 根据预约ID获取支付记录
     *
     * @param appointmentId 预约订单ID
     * @return 支付记录
     */
    Payment getByAppointmentId(Long appointmentId);

    /**
     * 获取支付信息
     *
     * @param userId        用户ID
     * @param appointmentId 预约订单ID
     * @return 支付信息
     */
    PaymentInfoVO getPaymentInfo(Long userId, Long appointmentId);

    /**
     * 模拟支付
     *
     * @param userId  用户ID
     * @param request 支付请求
     * @return 支付结果
     */
    PaymentResultVO pay(Long userId, PaymentPayRequest request);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付流水号
     * @return 支付记录
     */
    Payment getPaymentStatus(String paymentNo);

    /**
     * 申请退款
     *
     * @param userId  用户ID
     * @param request 退款请求
     * @return 是否成功
     */
    boolean refund(Long userId, PaymentRefundRequest request);
}
