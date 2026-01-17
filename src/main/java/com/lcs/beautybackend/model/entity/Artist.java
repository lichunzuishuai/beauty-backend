package com.lcs.beautybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * 化妆师表
 *
 * @TableName artist
 */
@TableName(value = "artist")
@Data
public class Artist implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 职称/头衔
     */
    private String title;

    /**
     * 个人简介
     */
    private String description;

    /**
     * 从业年限
     */
    private Integer experienceYears;

    /**
     * 擅长领域(JSON数组)
     */
    private String specialties;

    /**
     * 作品集图片(JSON数组)
     */
    private String portfolioImages;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 平均评分
     */
    private BigDecimal rating;

    /**
     * 接单数量
     */
    private Integer orderCount;

    /**
     * 起步价格
     */
    private BigDecimal basePrice;

    /**
     * 是否推荐: 0否 1是
     */
    private Integer isRecommend;

    /**
     * 状态: 0休息 1可接单
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}