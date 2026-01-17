package com.lcs.beautybackend.model.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付请求
 */
@Data
@Schema(description = "支付请求")
public class PaymentPayRequest implements Serializable {

    @Schema(description = "支付流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentNo;

    @Schema(description = "支付方式: 1微信 2支付宝 3模拟支付", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer paymentMethod;

    private static final long serialVersionUID = 1L;
}
