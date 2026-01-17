package com.lcs.beautybackend.model.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 退款请求
 */
@Data
@Schema(description = "退款请求")
public class PaymentRefundRequest implements Serializable {

    @Schema(description = "支付流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentNo;

    @Schema(description = "退款原因")
    private String refundReason;

    private static final long serialVersionUID = 1L;
}
