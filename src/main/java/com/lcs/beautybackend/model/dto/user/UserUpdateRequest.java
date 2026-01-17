package com.lcs.beautybackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息更新请求
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称", example = "小美")
    private String nickname;

    @Schema(description = "手机号", example = "13888888888")
    private String phone;

    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}
