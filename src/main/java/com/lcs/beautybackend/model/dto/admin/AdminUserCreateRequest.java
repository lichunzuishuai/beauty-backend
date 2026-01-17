package com.lcs.beautybackend.model.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员创建用户请求
 */
@Data
@Schema(description = "管理员创建用户请求")
public class AdminUserCreateRequest {
    @Schema(description = "用户名", required = true)
    private String username;

    @Schema(description = "密码", required = true)
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色：user普通用户 artist化妆师 admin管理员")
    private String role;

    @Schema(description = "状态：0禁用 1正常", defaultValue = "1")
    private Integer status = 1;
}
