package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付信息VO
 */
@Data
@Schema(description = "支付信息")
public class PaymentInfoVO implements Serializable {

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "预约订单ID")
    private Long appointmentId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "服务套餐名称")
    private String serviceName;

    @Schema(description = "化妆师姓名")
    private String artistName;

    @Schema(description = "支付状态: 0待支付 1已支付 2已退款")
    private Integer status;

    private static final long serialVersionUID = 1L;
}
