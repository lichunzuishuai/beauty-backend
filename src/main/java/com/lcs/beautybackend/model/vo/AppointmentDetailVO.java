package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约订单详情VO
 */
@Data
@Schema(description = "预约订单详情")
public class AppointmentDetailVO implements Serializable {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    // 用户信息
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户手机")
    private String userPhone;

    // 化妆师信息
    @Schema(description = "化妆师ID")
    private Long artistId;

    @Schema(description = "化妆师姓名")
    private String artistName;

    @Schema(description = "化妆师头像")
    private String artistAvatar;

    // 服务信息
    @Schema(description = "服务套餐ID")
    private Long serviceId;

    @Schema(description = "服务套餐名称")
    private String serviceName;

    @Schema(description = "服务价格")
    private BigDecimal servicePrice;

    @Schema(description = "服务时长（分钟）")
    private Integer serviceDuration;

    // 预约信息
    @Schema(description = "预约日期")
    private Date appointmentDate;

    @Schema(description = "预约时间")
    private String appointmentTime;

    @Schema(description = "服务地址")
    private String address;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态: 0待支付 1待确认 2已确认 3服务中 4已完成 5已取消 6已拒绝")
    private Integer status;

    @Schema(description = "取消/拒绝原因")
    private String cancelReason;

    // 支付信息
    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "支付状态: 0待支付 1已支付 2已退款")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    private Date paidAt;

    @Schema(description = "创建时间")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
