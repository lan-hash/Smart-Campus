package com.campus.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    // ========== 统计卡片 ==========
    /** 注册用户总数 */
    private Long userCount;
    /** 论坛帖子总数 */
    private Long postCount;
    /** 二手商品总数 */
    private Long productCount;
    /** 代课订单总数 */
    private Long orderCount;
    /** 待处理举报数 */
    private Long reportCount;

    // ========== 趋势百分比 ==========
    private Double userCountTrend;
    private Double postCountTrend;
    private Double productCountTrend;
    private Double orderCountTrend;
    private Double reportCountTrend;

    // ========== 图表数据 ==========
    /** 近7天新增用户（折线图） */
    private List<Long> userTrendData;
    /** 近7天活跃用户（折线图） */
    private List<Long> activeTrendData;
    /** 各模块数据占比（饼图） */
    private List<Map<String, Object>> moduleData;

    // ========== 待处理事项 ==========
    /** 待审核内容数 */
    private Long pendingReview;
}
