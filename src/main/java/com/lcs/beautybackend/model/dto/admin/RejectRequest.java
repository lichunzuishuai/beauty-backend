package com.lcs.beautybackend.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拒绝请求
 */
@Data
@Schema(description = "拒绝请求")
public class RejectRequest {
    @Schema(description = "拒绝原因", required = true)
    private String rejectReason;
}
