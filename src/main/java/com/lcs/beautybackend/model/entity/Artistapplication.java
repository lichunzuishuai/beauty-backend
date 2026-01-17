package com.lcs.beautybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 化妆师入驻申请表
 * @TableName artistapplication
 */
@TableName(value ="artistapplication")
@Data
public class Artistapplication implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 从业年限
     */
    private Integer experienceYears;

    /**
     * 擅长领域
     */
    private String specialties;

    /**
     * 资质证书图片(JSON数组)
     */
    private String certificateImages;

    /**
     * 作品集图片(JSON数组)
     */
    private String portfolioImages;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 状态: 0待审核 1通过 2拒绝
     */
    private Integer status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核人ID
     */
    private Long reviewedBy;

    /**
     * 审核时间
     */
    private Date reviewedAt;

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