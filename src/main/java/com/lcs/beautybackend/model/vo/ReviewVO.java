package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评价VO
 */
@Data
@Schema(description = "评价信息")
public class ReviewVO implements Serializable {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "评分(1-5)")
    private Integer rating;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价图片")
    private List<String> images;

    @Schema(description = "化妆师回复")
    private String reply;

    @Schema(description = "回复时间")
    private Date replyTime;

    @Schema(description = "评价时间")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
