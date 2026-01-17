package com.lcs.beautybackend.model.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改预约时间请求
 */
@Data
@Schema(description = "修改预约时间请求")
public class AppointmentRescheduleRequest implements Serializable {

    @Schema(description = "新预约日期 (格式: yyyy-MM-dd)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appointmentDate;

    @Schema(description = "新预约时间 (格式: HH:mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appointmentTime;

    private static final long serialVersionUID = 1L;
}
