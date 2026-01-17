package com.lcs.beautybackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台统计数据VO
 */
@Data
@Schema(description = "管理后台统计数据")
public class AdminStatsVO {
    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "化妆师总数")
    private Long artistCount;

    @Schema(description = "预约订单总数")
    private Long appointmentCount;

    @Schema(description = "待审核入驻申请数")
    private Long pendingApplications;

    @Schema(description = "待确认预约数")
    private Long pendingAppointments;

    @Schema(description = "今日预约数")
    private Long todayAppointments;

    @Schema(description = "总收入")
    private Double totalRevenue;
}
