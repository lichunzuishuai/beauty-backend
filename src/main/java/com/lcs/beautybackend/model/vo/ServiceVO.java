package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 服务套餐视图对象
 */
@Data
@Schema(description = "服务套餐信息")
public class ServiceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务套餐ID")
    private Long id;

    @Schema(description = "化妆师ID")
    private Long artistId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

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

    @Schema(description = "详情图片列表")
    private List<String> images;

    @Schema(description = "状态: 0下架 1上架")
    private Integer status;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
