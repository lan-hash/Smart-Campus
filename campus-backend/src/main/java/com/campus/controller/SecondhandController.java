package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.CreateProductRequest;
import com.campus.dto.TransactionRequest;
import com.campus.entity.SecondhandCategory;
import com.campus.service.SecondhandService;
import com.campus.vo.SecondhandProductVO;
import com.campus.vo.SecondhandTransactionVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/secondhand")
@RequiredArgsConstructor
public class SecondhandController {

    private final SecondhandService secondhandService;

    /** 分类列表（无需登录） */
    @GetMapping("/categories")
    public Result<List<SecondhandCategory>> getCategories() {
        return Result.success(secondhandService.getCategories());
    }

    /** 商品分页列表（无需登录，只返回在售商品） */
    @GetMapping("/products")
    public Result<PageResult<SecondhandProductVO>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SecondhandProductVO> result = secondhandService.getProductPage(categoryId, keyword, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 商品详情（无需登录，浏览量+1） */
    @GetMapping("/products/{id}")
    public Result<SecondhandProductVO> getProductDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(secondhandService.getProductDetail(id, userId));
    }

    /** 发布商品（需登录） */
    @PostMapping("/publish")
    public Result<Void> createProduct(HttpServletRequest request,
                                      @RequestBody @Valid CreateProductRequest createProductRequest) {
        Long userId = (Long) request.getAttribute("userId");
        secondhandService.createProduct(userId, createProductRequest);
        return Result.success();
    }

    /** 编辑商品（只能编辑自己的，需登录） */
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody @Valid CreateProductRequest createProductRequest) {
        Long userId = (Long) request.getAttribute("userId");
        secondhandService.updateProduct(userId, id, createProductRequest);
        return Result.success();
    }

    /** 修改商品状态（上架/下架/标记已售，需登录） */
    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(HttpServletRequest request,
                                             @PathVariable Long id,
                                             @RequestParam Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        secondhandService.updateProductStatus(userId, id, status);
        return Result.success();
    }

    /** 删除商品（只能删除自己的，需登录） */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        secondhandService.deleteProduct(userId, id);
        return Result.success();
    }

    /** 收藏/取消收藏（toggle逻辑，需登录） */
    @PostMapping("/products/{id}/favorite")
    public Result<Boolean> toggleFavorite(HttpServletRequest request,
                                          @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = secondhandService.toggleFavorite(userId, id);
        return Result.success(favorited);
    }

    /** 发起购买（创建交易记录，需登录） */
    @PostMapping("/products/{id}/buy")
    public Result<Void> buyProduct(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setProductId(id);
        secondhandService.buyProduct(userId, transactionRequest);
        return Result.success();
    }

    /** 我的发布（需登录） */
    @GetMapping("/my/products")
    public Result<PageResult<SecondhandProductVO>> getMyProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<SecondhandProductVO> result = secondhandService.getMyProducts(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 我的收藏（需登录） */
    @GetMapping("/my/favorites")
    public Result<PageResult<SecondhandProductVO>> getMyFavorites(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<SecondhandProductVO> result = secondhandService.getMyFavorites(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 我的交易记录（买/卖，需登录） */
    @GetMapping("/transactions")
    public Result<PageResult<SecondhandTransactionVO>> getMyTransactions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<SecondhandTransactionVO> result = secondhandService.getMyTransactions(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 更新交易状态（交易双方可操作，需登录） */
    @PutMapping("/transactions/{id}/status")
    public Result<Void> updateTransactionStatus(HttpServletRequest request,
                                                 @PathVariable Long id,
                                                 @RequestParam Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        secondhandService.updateTransactionStatus(userId, id, status);
        return Result.success();
    }
}
