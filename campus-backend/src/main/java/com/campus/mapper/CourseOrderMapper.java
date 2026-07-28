package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.CourseOrder;
import com.campus.vo.CourseOrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface CourseOrderMapper extends BaseMapper<CourseOrder> {

    /**
     * 分页查询订单列表，LEFT JOIN sys_user获取发布者和接单者信息
     */
    Page<CourseOrderVO> selectOrderPage(
            Page<?> page,
            @Param("status") Integer status
    );

    /**
     * 查询订单详情，LEFT JOIN sys_user获取发布者和接单者信息
     */
    CourseOrderVO selectOrderDetail(@Param("orderId") Long orderId);

    /**
     * 查询我的订单（作为发布者或接单者）
     */
    Page<CourseOrderVO> selectMyOrderPage(
            Page<?> page,
            @Param("userId") Long userId
    );

    /**
     * 浏览量+1
     */
    @Update("UPDATE course_order SET view_count = view_count + 1 WHERE id = #{orderId}")
    int incrementViewCount(@Param("orderId") Long orderId);
}
