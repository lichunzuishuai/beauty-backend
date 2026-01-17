package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 化妆师视图（已脱敏）
 */
@Data
@Schema(description = "化妆师信息（已脱敏）")
public class ArtistVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "化妆师ID")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "职称/头衔")
    private String title;

    @Schema(description = "个人简介")
    private String description;

    @Schema(description = "从业年限")
    private Integer experienceYears;

    @Schema(description = "擅长领域")
    private List<String> specialties;

    @Schema(description = "作品集图片")
    private List<String> portfolioImages;

    @Schema(description = "平均评分")
    private BigDecimal rating;

    @Schema(description = "接单数量")
    private Integer orderCount;

    @Schema(description = "起步价格")
    private BigDecimal basePrice;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "状态: 0休息 1可接单")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    // region 关联用户信息（脱敏）

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    // endregion
}
