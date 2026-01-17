package com.lcs.beautybackend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.mapper.AppointmentMapper;
import com.lcs.beautybackend.model.dto.appointment.AppointmentCreateRequest;
import com.lcs.beautybackend.model.dto.appointment.AppointmentRescheduleRequest;
import com.lcs.beautybackend.model.entity.*;
import com.lcs.beautybackend.model.vo.AppointmentDetailVO;
import com.lcs.beautybackend.model.vo.AppointmentVO;
import com.lcs.beautybackend.service.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lcs
 * @description 针对表【appointment(预约订单表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@org.springframework.stereotype.Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {

    @Resource
    private ArtistService artistService;

    @Resource
    private ServiceService serviceService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private UserService userService;

    @Override
    public AppointmentVO createAppointment(Long userId, AppointmentCreateRequest request) {
        // 参数校验
        if (request.getArtistId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择化妆师");
        }
        if (request.getServiceId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择服务套餐");
        }
        if (StringUtils.isBlank(request.getAppointmentDate())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择预约日期");
        }
        if (StringUtils.isBlank(request.getAppointmentTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择预约时间");
        }

        // 查询化妆师
        Artist artist = artistService.getById(request.getArtistId());
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }
        if (artist.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该化妆师当前不可接单");
        }

        // 查询服务套餐
        com.lcs.beautybackend.model.entity.Service service = serviceService.getById(request.getServiceId());
        if (service == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }
        if (service.getStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该服务套餐已下架");
        }
        if (!service.getArtistId().equals(request.getArtistId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "服务套餐与化妆师不匹配");
        }

        // 生成订单号
        String orderNo = "ORD" + DateUtil.format(new Date(), "yyyyMMddHHmmss")
                + IdUtil.getSnowflakeNextIdStr().substring(10);

        // 解析日期和时间
        Date appointmentDate;
        Date appointmentTime;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            appointmentDate = dateFormat.parse(request.getAppointmentDate());
            appointmentTime = timeFormat.parse(request.getAppointmentTime());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期或时间格式错误");
        }

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setOrderNo(orderNo);
        appointment.setUserId(userId);
        appointment.setArtistId(request.getArtistId());
        appointment.setServiceId(request.getServiceId());
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setAddress(request.getAddress());
        appointment.setContactName(request.getContactName());
        appointment.setContactPhone(request.getContactPhone());
        appointment.setRemark(request.getRemark());
        appointment.setTotalAmount(service.getPrice());
        appointment.setStatus(0); // 待支付

        boolean saved = this.save(appointment);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建预约失败");
        }

        // 创建支付记录
        paymentService.createPayment(appointment.getId(), service.getPrice());

        // 返回VO
        return convertToVO(appointment, artist.getRealName(), service.getName());
    }

    @Override
    public boolean cancelAppointment(Long userId, Long id, String reason) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        }
        // 只有待支付、待确认、已确认状态可以取消
        if (appointment.getStatus() > 2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前状态不可取消");
        }

        appointment.setStatus(5); // 已取消
        appointment.setCancelReason(reason);
        return this.updateById(appointment);
    }

    @Override
    public boolean rescheduleAppointment(Long userId, Long id, AppointmentRescheduleRequest request) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        }
        // 只有待确认、已确认状态可以修改时间
        if (appointment.getStatus() != 1 && appointment.getStatus() != 2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前状态不可修改时间");
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            appointment.setAppointmentDate(dateFormat.parse(request.getAppointmentDate()));
            appointment.setAppointmentTime(timeFormat.parse(request.getAppointmentTime()));
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期或时间格式错误");
        }

        return this.updateById(appointment);
    }

    @Override
    public Page<AppointmentVO> getMyAppointments(Long userId, Integer status, int current, int pageSize) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(Appointment::getStatus, status);
        }
        queryWrapper.orderByDesc(Appointment::getCreateTime);

        Page<Appointment> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<AppointmentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<AppointmentVO> voList = page.getRecords().stream().map(appointment -> {
            Artist artist = artistService.getById(appointment.getArtistId());
            com.lcs.beautybackend.model.entity.Service service = serviceService.getById(appointment.getServiceId());
            return convertToVO(appointment,
                    artist != null ? artist.getRealName() : "未知",
                    service != null ? service.getName() : "未知");
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AppointmentDetailVO getAppointmentDetail(Long userId, Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看此预约");
        }

        // 查询关联数据
        User user = userService.getById(appointment.getUserId());
        Artist artist = artistService.getById(appointment.getArtistId());
        com.lcs.beautybackend.model.entity.Service service = serviceService.getById(appointment.getServiceId());
        Payment payment = paymentService.getByAppointmentId(appointment.getId());

        // 构建详情VO
        AppointmentDetailVO vo = new AppointmentDetailVO();
        vo.setId(appointment.getId());
        vo.setOrderNo(appointment.getOrderNo());

        // 用户信息
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setUserPhone(user.getPhone());

        // 化妆师信息
        if (artist != null) {
            vo.setArtistId(artist.getId());
            vo.setArtistName(artist.getRealName());
            // 获取化妆师用户头像
            User artistUser = userService.getById(artist.getUserId());
            if (artistUser != null) {
                vo.setArtistAvatar(artistUser.getAvatar());
            }
        }

        // 服务信息
        if (service != null) {
            vo.setServiceId(service.getId());
            vo.setServiceName(service.getName());
            vo.setServicePrice(service.getPrice());
            vo.setServiceDuration(service.getDuration());
        }

        // 预约信息
        vo.setAppointmentDate(appointment.getAppointmentDate());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        vo.setAppointmentTime(timeFormat.format(appointment.getAppointmentTime()));
        vo.setAddress(appointment.getAddress());
        vo.setContactName(appointment.getContactName());
        vo.setContactPhone(appointment.getContactPhone());
        vo.setRemark(appointment.getRemark());
        vo.setTotalAmount(appointment.getTotalAmount());
        vo.setStatus(appointment.getStatus());
        vo.setCancelReason(appointment.getCancelReason());
        vo.setCreateTime(appointment.getCreateTime());

        // 支付信息
        if (payment != null) {
            vo.setPaymentNo(payment.getPaymentNo());
            vo.setPaymentStatus(payment.getStatus());
            vo.setPaidAt(payment.getPaidAt());
        }

        return vo;
    }

    private AppointmentVO convertToVO(Appointment appointment, String artistName, String serviceName) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(appointment.getId());
        vo.setOrderNo(appointment.getOrderNo());
        vo.setArtistId(appointment.getArtistId());
        vo.setArtistName(artistName);
        vo.setServiceId(appointment.getServiceId());
        vo.setServiceName(serviceName);
        vo.setAppointmentDate(appointment.getAppointmentDate());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        vo.setAppointmentTime(timeFormat.format(appointment.getAppointmentTime()));
        vo.setTotalAmount(appointment.getTotalAmount());
        vo.setStatus(appointment.getStatus());
        vo.setCreateTime(appointment.getCreateTime());
        return vo;
    }
}
