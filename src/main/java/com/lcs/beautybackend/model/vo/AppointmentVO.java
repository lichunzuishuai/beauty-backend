package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约订单VO（列表用）
 */
@Data
@Schema(description = "预约订单信息")
public class AppointmentVO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "化妆师ID")
    private Long artistId;

    @Schema(description = "化妆师姓名")
    private String artistName;

    @Schema(description = "服务套餐ID")
    private Long serviceId;

    @Schema(description = "服务套餐名称")
    private String serviceName;

    @Schema(description = "预约日期")
    private Date appointmentDate;

    @Schema(description = "预约时间")
    private String appointmentTime;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态: 0待支付 1待确认 2已确认 3服务中 4已完成 5已取消 6已拒绝")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
