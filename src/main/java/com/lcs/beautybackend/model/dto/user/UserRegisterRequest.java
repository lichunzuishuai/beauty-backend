package com.lcs.beautybackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;

    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String confirmPassword;

    @Schema(description = "手机号", example = "13888888888")
    private String phone;
}
