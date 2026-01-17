package com.lcs.beautybackend.model.dto.artistapplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 化妆师入驻申请请求
 */
@Data
@Schema(description = "化妆师入驻申请请求")
public class ArtistApplyRequest implements Serializable {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "从业年限")
    private Integer experienceYears;

    @Schema(description = "擅长领域，多个用逗号分隔")
    private String specialties;

    @Schema(description = "资质证书图片URL列表")
    private List<String> certificateImages;

    @Schema(description = "作品集图片URL列表")
    private List<String> portfolioImages;

    @Schema(description = "个人介绍")
    private String introduction;

    private static final long serialVersionUID = 1L;
}
