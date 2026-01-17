package com.lcs.beautybackend.model.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建评价请求
 */
@Data
@Schema(description = "创建评价请求")
public class ReviewCreateRequest implements Serializable {

    @Schema(description = "预约订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long appointmentId;

    @Schema(description = "评分(1-5)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价图片URL列表")
    private List<String> images;

    private static final long serialVersionUID = 1L;
}
