package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付结果VO
 */
@Data
@Schema(description = "支付结果")
public class PaymentResultVO implements Serializable {

    @Schema(description = "是否支付成功")
    private Boolean success;

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "第三方交易号")
    private String tradeNo;

    @Schema(description = "支付时间")
    private Date paidAt;

    @Schema(description = "提示信息")
    private String message;

    private static final long serialVersionUID = 1L;
}
