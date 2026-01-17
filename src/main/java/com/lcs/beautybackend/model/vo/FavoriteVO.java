package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏VO
 */
@Data
@Schema(description = "收藏信息")
public class FavoriteVO implements Serializable {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "收藏类型: 1化妆师 2服务套餐")
    private Integer targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标名称")
    private String targetName;

    @Schema(description = "目标图片")
    private String targetImage;

    @Schema(description = "收藏时间")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
