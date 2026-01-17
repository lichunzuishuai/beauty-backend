package com.lcs.beautybackend.model.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建预约请求
 */
@Data
@Schema(description = "创建预约请求")
public class AppointmentCreateRequest implements Serializable {

    @Schema(description = "化妆师ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long artistId;

    @Schema(description = "服务套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serviceId;

    @Schema(description = "预约日期 (格式: yyyy-MM-dd)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appointmentDate;

    @Schema(description = "预约时间 (格式: HH:mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appointmentTime;

    @Schema(description = "服务地址")
    private String address;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}
