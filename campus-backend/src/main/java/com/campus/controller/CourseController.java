package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.CreateOrderRequest;
import com.campus.service.CourseService;
import com.campus.vo.CourseEvaluationVO;
import com.campus.vo.CourseOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** 订单分页列表（无需登录，待接单+进行中的在前） */
    @GetMapping("/orders")
    public Result<PageResult<CourseOrderVO>> getOrderPage(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CourseOrderVO> result = courseService.getOrderPage(status, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 订单详情（无需登录，浏览量+1） */
    @GetMapping("/orders/{id}")
    public Result<CourseOrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(courseService.getOrderDetail(id));
    }

    /** 发布代课需求（需登录） */
    @PostMapping("/create")
    public Result<Void> createOrder(HttpServletRequest request,
                                    @RequestBody @Valid CreateOrderRequest createOrderRequest) {
        Long userId = (Long) request.getAttribute("userId");
        courseService.createOrder(userId, createOrderRequest);
        return Result.success();
    }

    /** 接单（需登录） */
    @PostMapping("/orders/{id}/accept")
    public Result<Void> acceptOrder(HttpServletRequest request,
                                    @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        courseService.acceptOrder(userId, id);
        return Result.success();
    }

    /** 完成订单（需登录，发布者确认完成） */
    @PutMapping("/orders/{id}/complete")
    public Result<Void> completeOrder(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        courseService.completeOrder(userId, id);
        return Result.success();
    }

    /** 取消订单（需登录，发布者可取消待接单/进行中） */
    @PutMapping("/orders/{id}/cancel")
    public Result<Void> cancelOrder(HttpServletRequest request,
                                    @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        courseService.cancelOrder(userId, id);
        return Result.success();
    }

    /** 评价（需登录，完成后评价，只能评价对方） */
    @PostMapping("/orders/{id}/evaluate")
    public Result<Void> evaluateOrder(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestParam Integer score,
                                      @RequestParam(required = false) String content) {
        Long userId = (Long) request.getAttribute("userId");
        courseService.evaluateOrder(userId, id, score, content);
        return Result.success();
    }

    /** 获取某订单的评价列表（无需登录） */
    @GetMapping("/orders/{id}/evaluations")
    public Result<List<CourseEvaluationVO>> getEvaluations(@PathVariable Long id) {
        return Result.success(courseService.getEvaluations(id));
    }

    /** 我的订单（需登录，作为发布者或接单者） */
    @GetMapping("/my/orders")
    public Result<PageResult<CourseOrderVO>> getMyOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<CourseOrderVO> result = courseService.getMyOrders(userId, page, size);
        return Result.success(PageResult.of(result));
    }
}
