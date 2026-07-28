package com.campus.vo;

import lombok.Data;

@Data
public class DashboardVO {

    /** 总用户数 */
    private Long totalUsers;

    /** 今日新增用户 */
    private Long todayNewUsers;

    /** 总帖子数 */
    private Long totalPosts;

    /** 总商品数 */
    private Long totalProducts;

    /** 总代课订单数 */
    private Long totalCourseOrders;

    /** 总表白数 */
    private Long totalConfessions;

    /** 待处理举报数 */
    private Long pendingReports;
}
