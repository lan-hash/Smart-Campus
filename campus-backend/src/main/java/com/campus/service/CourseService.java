package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.CreateOrderRequest;
import com.campus.vo.CourseEvaluationVO;
import com.campus.vo.CourseOrderVO;

import java.util.List;

public interface CourseService {

    /** 订单分页列表（公开，支持status筛选，待接单+进行中的在前） */
    Page<CourseOrderVO> getOrderPage(Integer status, int page, int size);

    /** 订单详情（浏览量+1） */
    CourseOrderVO getOrderDetail(Long orderId);

    /** 发布代课需求 */
    void createOrder(Long userId, CreateOrderRequest request);

    /** 接单（不能接自己的单，订单必须是待接单状态） */
    void acceptOrder(Long userId, Long orderId);

    /** 完成订单（只有发布者可确认完成） */
    void completeOrder(Long userId, Long orderId);

    /** 取消订单（发布者可取消待接单/进行中的订单） */
    void cancelOrder(Long userId, Long orderId);

    /** 评价（完成后评价，from_user只能评价对方） */
    void evaluateOrder(Long userId, Long orderId, Integer score, String content);

    /** 获取某订单的评价列表 */
    List<CourseEvaluationVO> getEvaluations(Long orderId);

    /** 我的订单（作为发布者或接单者） */
    Page<CourseOrderVO> getMyOrders(Long userId, int page, int size);
}
