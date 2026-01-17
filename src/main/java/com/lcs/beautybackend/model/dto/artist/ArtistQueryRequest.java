package com.lcs.beautybackend.model.dto.artist;

import com.lcs.beautybackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 化妆师查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "化妆师查询请求")
public class ArtistQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "姓名（模糊搜索）")
    private String realName;

    @Schema(description = "擅长领域")
    private String specialties;

    @Schema(description = "最低评分")
    private BigDecimal minRating;

    @Schema(description = "最高评分")
    private BigDecimal maxRating;

    @Schema(description = "状态: 0休息 1可接单")
    private Integer status;

    @Schema(description = "是否推荐")
    private Integer isRecommend;
}
