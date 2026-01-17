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
 * 服务套餐表
 * @TableName service
 */
@TableName(value ="service")
@Data
public class Service implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 化妆师ID
     */
    private Long artistId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 服务时长(分钟)
     */
    private Integer duration;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 详情图片(JSON数组)
     */
    private String images;

    /**
     * 状态: 0下架 1上架
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sortOrder;

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