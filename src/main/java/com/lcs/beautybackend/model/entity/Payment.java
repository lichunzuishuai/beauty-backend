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
 * 支付记录表
 * @TableName payment
 */
@TableName(value ="payment")
@Data
public class Payment implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约订单ID
     */
    private Long appointmentId;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 第三方交易号
     */
    private String tradeNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式: 1微信 2支付宝 3模拟支付
     */
    private Integer paymentMethod;

    /**
     * 状态: 0待支付 1已支付 2已退款
     */
    private Integer status;

    /**
     * 支付时间
     */
    private Date paidAt;

    /**
     * 退款时间
     */
    private Date refundAt;

    /**
     * 退款原因
     */
    private String refundReason;

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