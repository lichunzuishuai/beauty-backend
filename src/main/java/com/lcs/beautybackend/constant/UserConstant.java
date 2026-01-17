package com.lcs.beautybackend.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    // region 角色常量

    /**
     * 默认角色（普通用户）
     */
    int DEFAULT_ROLE = 0;

    /**
     * 化妆师角色
     */
    int ARTIST_ROLE = 1;

    /**
     * 管理员角色
     */
    int ADMIN_ROLE = 2;

    // endregion

    // region 状态常量

    /**
     * 用户状态：禁用
     */
    int STATUS_DISABLED = 0;

    /**
     * 用户状态：正常
     */
    int STATUS_NORMAL = 1;

    // endregion
}
