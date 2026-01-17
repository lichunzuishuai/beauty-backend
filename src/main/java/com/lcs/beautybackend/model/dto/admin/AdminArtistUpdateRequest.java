package com.lcs.beautybackend.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员更新化妆师请求
 */
@Data
@Schema(description = "管理员更新化妆师请求")
public class AdminArtistUpdateRequest {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "从业年限")
    private Integer experienceYears;

    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "作品集")
    private String portfolio;

    @Schema(description = "头像")
    private String avatar;
}
