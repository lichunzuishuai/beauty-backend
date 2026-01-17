package com.lcs.beautybackend.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 状态更新请求
 */
@Data
@Schema(description = "状态更新请求")
public class StatusUpdateRequest {
    @Schema(description = "状态值")
    private Integer status;
}
