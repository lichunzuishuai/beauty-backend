package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录用户视图（已脱敏）
 */
@Data
@Schema(description = "登录用户信息（已脱敏）")
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号（已脱敏）", example = "138****8888")
    private String phone;

    @Schema(description = "邮箱（已脱敏）", example = "t***@example.com")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "角色: 0用户 1化妆师 2管理员")
    private Integer role;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "JWT Token（用于后续请求认证）")
    private String token;
}
