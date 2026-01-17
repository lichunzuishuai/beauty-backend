package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 化妆师入驻申请状态VO
 */
@Data
@Schema(description = "化妆师入驻申请状态")
public class ArtistApplicationVO implements Serializable {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "状态: 0待审核 1通过 2拒绝")
    private Integer status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private Date createTime;

    @Schema(description = "审核时间")
    private Date reviewedAt;

    private static final long serialVersionUID = 1L;
}
