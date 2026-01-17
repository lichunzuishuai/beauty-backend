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
 * 预约订单表
 * @TableName appointment
 */
@TableName(value ="appointment")
@Data
public class Appointment implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 化妆师ID
     */
    private Long artistId;

    /**
     * 服务套餐ID
     */
    private Long serviceId;

    /**
     * 预约日期
     */
    private Date appointmentDate;

    /**
     * 预约时间
     */
    private Date appointmentTime;

    /**
     * 服务地址
     */
    private String address;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0待支付 1待确认 2已确认 3服务中 4已完成 5已取消 6已拒绝
     */
    private Integer status;

    /**
     * 取消/拒绝原因
     */
    private String cancelReason;

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