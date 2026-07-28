package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.dto.CreateOrderRequest;
import com.campus.entity.CourseEvaluation;
import com.campus.entity.CourseOrder;
import com.campus.mapper.CourseEvaluationMapper;
import com.campus.mapper.CourseOrderMapper;
import com.campus.service.CourseService;
import com.campus.vo.CourseEvaluationVO;
import com.campus.vo.CourseOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseOrderMapper courseOrderMapper;
    private final CourseEvaluationMapper courseEvaluationMapper;

    @Override
    public Page<CourseOrderVO> getOrderPage(Integer status, int page, int size) {
        Page<CourseOrderVO> pageParam = new Page<>(page, size);
        return courseOrderMapper.selectOrderPage(pageParam, status);
    }

    @Override
    @Transactional
    public CourseOrderVO getOrderDetail(Long orderId) {
        CourseOrderVO orderVO = courseOrderMapper.selectOrderDetail(orderId);
        if (orderVO == null) {
            throw new BusinessException("订单不存在");
        }
        // 浏览量+1
        courseOrderMapper.incrementViewCount(orderId);
        return orderVO;
    }

    @Override
    public void createOrder(Long userId, CreateOrderRequest request) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        CourseOrder order = new CourseOrder();
        order.setUserId(userId);
        order.setCourseName(request.getCourseName());
        order.setCourseType(request.getCourseType());
        order.setClassTime(request.getClassTime());
        order.setLocation(request.getLocation());
        order.setSalary(request.getSalary());
        order.setDescription(request.getDescription());
        order.setContact(request.getContact());
        order.setStatus(0);
        order.setViewCount(0);
        courseOrderMapper.insert(order);
    }

    @Override
    @Transactional
    public void acceptOrder(Long userId, Long orderId) {
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 不能接自己的单
        if (order.getUserId().equals(userId)) {
            throw new BusinessException("不能接自己的订单");
        }
        // 订单必须是待接单状态
        if (order.getStatus() != 0) {
            throw new BusinessException("该订单当前状态不可接单");
        }
        order.setAcceptUserId(userId);
        order.setStatus(1);
        courseOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long userId, Long orderId) {
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 只有发布者可确认完成
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("只有发布者可以确认完成订单");
        }
        // 订单必须是进行中状态
        if (order.getStatus() != 1) {
            throw new BusinessException("该订单当前状态不可完成");
        }
        order.setStatus(2);
        courseOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 只有发布者可取消
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("只有发布者可以取消订单");
        }
        // 只能取消待接单或进行中的订单
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("该订单当前状态不可取消");
        }
        order.setStatus(3);
        courseOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void evaluateOrder(Long userId, Long orderId, Integer score, String content) {
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 只有订单完成后才能评价
        if (order.getStatus() != 2) {
            throw new BusinessException("只有已完成的订单才能评价");
        }
        // 评价人必须是发布者或接单者
        if (!order.getUserId().equals(userId) && !order.getAcceptUserId().equals(userId)) {
            throw new BusinessException("只有订单双方才能评价");
        }
        // 评价对象是对方
        Long toUserId;
        if (order.getUserId().equals(userId)) {
            toUserId = order.getAcceptUserId();
        } else {
            toUserId = order.getUserId();
        }
        // 检查是否已经评价过
        LambdaQueryWrapper<CourseEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseEvaluation::getOrderId, orderId)
               .eq(CourseEvaluation::getFromUserId, userId);
        Long count = courseEvaluationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("你已经评价过该订单");
        }
        // 创建评价
        CourseEvaluation evaluation = new CourseEvaluation();
        evaluation.setOrderId(orderId);
        evaluation.setFromUserId(userId);
        evaluation.setToUserId(toUserId);
        evaluation.setScore(score);
        evaluation.setContent(content);
        courseEvaluationMapper.insert(evaluation);
    }

    @Override
    public List<CourseEvaluationVO> getEvaluations(Long orderId) {
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return courseEvaluationMapper.selectEvaluationsByOrderId(orderId);
    }

    @Override
    public Page<CourseOrderVO> getMyOrders(Long userId, int page, int size) {
        Page<CourseOrderVO> pageParam = new Page<>(page, size);
        return courseOrderMapper.selectMyOrderPage(pageParam, userId);
    }
}
