package com.lcs.beautybackend.model.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消预约请求
 */
@Data
@Schema(description = "取消预约请求")
public class AppointmentCancelRequest implements Serializable {

    @Schema(description = "取消原因")
    private String cancelReason;

    private static final long serialVersionUID = 1L;
}
