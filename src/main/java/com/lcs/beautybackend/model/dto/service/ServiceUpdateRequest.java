package com.lcs.beautybackend.model.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 修改服务套餐请求
 */
@Data
@Schema(description = "修改服务套餐请求")
public class ServiceUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐描述")
    private String description;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "服务时长（分钟）")
    private Integer duration;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "详情图片数组")
    private List<String> images;

    @Schema(description = "排序权重")
    private Integer sortOrder;
}
