package com.lcs.beautybackend.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 */
public class DesensitizeUtils {

    /**
     * 手机号脱敏
     * 规则：保留前3位和后4位，中间用****代替
     * 例如：138****8888
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏
     * 规则：保留第一个字符和@后面的内容，中间用***代替
     * 例如：t***@example.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 身份证号脱敏
     * 规则：保留前3位和后4位，中间用*代替
     * 例如：110***********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 7) {
            return idCard;
        }
        int length = idCard.length();
        StringBuilder sb = new StringBuilder();
        sb.append(idCard, 0, 3);
        for (int i = 0; i < length - 7; i++) {
            sb.append("*");
        }
        sb.append(idCard.substring(length - 4));
        return sb.toString();
    }

    /**
     * 姓名脱敏
     * 规则：保留第一个字，其余用*代替
     * 例如：张** 或 李*
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }
}
