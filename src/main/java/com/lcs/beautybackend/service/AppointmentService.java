package com.lcs.beautybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.model.dto.appointment.AppointmentCreateRequest;
import com.lcs.beautybackend.model.dto.appointment.AppointmentRescheduleRequest;
import com.lcs.beautybackend.model.entity.Appointment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.AppointmentDetailVO;
import com.lcs.beautybackend.model.vo.AppointmentVO;

/**
 * @author lcs
 * @description 针对表【appointment(预约订单表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     *
     * @param userId  用户ID
     * @param request 预约信息
     * @return 预约订单信息
     */
    AppointmentVO createAppointment(Long userId, AppointmentCreateRequest request);

    /**
     * 取消预约
     *
     * @param userId 用户ID
     * @param id     预约ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelAppointment(Long userId, Long id, String reason);

    /**
     * 修改预约时间
     *
     * @param userId  用户ID
     * @param id      预约ID
     * @param request 新时间
     * @return 是否成功
     */
    boolean rescheduleAppointment(Long userId, Long id, AppointmentRescheduleRequest request);

    /**
     * 查询我的预约列表
     *
     * @param userId   用户ID
     * @param status   状态筛选
     * @param current  当前页
     * @param pageSize 每页数量
     * @return 预约列表
     */
    Page<AppointmentVO> getMyAppointments(Long userId, Integer status, int current, int pageSize);

    /**
     * 查询预约详情
     *
     * @param userId 用户ID
     * @param id     预约ID
     * @return 预约详情
     */
    AppointmentDetailVO getAppointmentDetail(Long userId, Long id);
}
