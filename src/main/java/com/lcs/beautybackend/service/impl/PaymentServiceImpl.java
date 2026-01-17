package com.lcs.beautybackend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.mapper.PaymentMapper;
import com.lcs.beautybackend.model.dto.payment.PaymentPayRequest;
import com.lcs.beautybackend.model.dto.payment.PaymentRefundRequest;
import com.lcs.beautybackend.model.entity.Appointment;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.Payment;
import com.lcs.beautybackend.model.vo.PaymentInfoVO;
import com.lcs.beautybackend.model.vo.PaymentResultVO;
import com.lcs.beautybackend.service.AppointmentService;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.PaymentService;
import com.lcs.beautybackend.service.ServiceService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lcs
 * @description 针对表【payment(支付记录表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment>
        implements PaymentService {

    @Resource
    @Lazy
    private AppointmentService appointmentService;

    @Resource
    private ArtistService artistService;

    @Resource
    private ServiceService serviceService;

    @Override
    public Payment createPayment(Long appointmentId, BigDecimal amount) {
        // 生成支付流水号
        String paymentNo = "PAY" + DateUtil.format(new Date(), "yyyyMMddHHmmss")
                + IdUtil.getSnowflakeNextIdStr().substring(10);

        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPaymentNo(paymentNo);
        payment.setAmount(amount);
        payment.setPaymentMethod(3); // 默认模拟支付
        payment.setStatus(0); // 待支付

        this.save(payment);
        return payment;
    }

    @Override
    public Payment getByAppointmentId(Long appointmentId) {
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getAppointmentId, appointmentId);
        return this.getOne(queryWrapper);
    }

    @Override
    public PaymentInfoVO getPaymentInfo(Long userId, Long appointmentId) {
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看此支付信息");
        }

        Payment payment = getByAppointmentId(appointmentId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        // 查询关联信息
        Artist artist = artistService.getById(appointment.getArtistId());
        com.lcs.beautybackend.model.entity.Service service = serviceService.getById(appointment.getServiceId());

        PaymentInfoVO vo = new PaymentInfoVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setAppointmentId(appointmentId);
        vo.setOrderNo(appointment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setServiceName(service != null ? service.getName() : "未知服务");
        vo.setArtistName(artist != null ? artist.getRealName() : "未知化妆师");

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO pay(Long userId, PaymentPayRequest request) {
        if (StringUtils.isBlank(request.getPaymentNo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付流水号不能为空");
        }

        // 查询支付记录
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getPaymentNo, request.getPaymentNo());
        Payment payment = this.getOne(queryWrapper);

        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        // 验证订单归属
        Appointment appointment = appointmentService.getById(payment.getAppointmentId());
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此支付");
        }

        if (payment.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已支付或已退款");
        }

        PaymentResultVO result = new PaymentResultVO();

        // 模拟支付（在真实场景中这里会调用第三方支付接口）
        try {
            // 生成模拟交易号
            String tradeNo = "TRADE" + System.currentTimeMillis();

            // 更新支付记录
            payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : 3);
            payment.setStatus(1); // 已支付
            payment.setTradeNo(tradeNo);
            payment.setPaidAt(new Date());
            this.updateById(payment);

            // 更新预约状态为待确认
            appointment.setStatus(1); // 待确认
            appointmentService.updateById(appointment);

            result.setSuccess(true);
            result.setPaymentNo(payment.getPaymentNo());
            result.setTradeNo(tradeNo);
            result.setPaidAt(payment.getPaidAt());
            result.setMessage("支付成功");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setPaymentNo(payment.getPaymentNo());
            result.setMessage("支付失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Payment getPaymentStatus(String paymentNo) {
        if (StringUtils.isBlank(paymentNo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付流水号不能为空");
        }
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getPaymentNo, paymentNo);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refund(Long userId, PaymentRefundRequest request) {
        if (StringUtils.isBlank(request.getPaymentNo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付流水号不能为空");
        }

        // 查询支付记录
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getPaymentNo, request.getPaymentNo());
        Payment payment = this.getOne(queryWrapper);

        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        // 验证订单归属
        Appointment appointment = appointmentService.getById(payment.getAppointmentId());
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作退款");
        }

        if (payment.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单未支付或已退款");
        }

        // 只有待确认、已拒绝状态可以退款
        if (appointment.getStatus() != 1 && appointment.getStatus() != 6) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前订单状态不支持退款");
        }

        // 模拟退款（在真实场景中这里会调用第三方支付退款接口）
        payment.setStatus(2); // 已退款
        payment.setRefundAt(new Date());
        payment.setRefundReason(request.getRefundReason());
        this.updateById(payment);

        // 更新订单状态
        appointment.setStatus(5); // 已取消
        appointment.setCancelReason("用户申请退款");
        appointmentService.updateById(appointment);

        return true;
    }
}
