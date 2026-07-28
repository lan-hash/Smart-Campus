package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.CreateProductRequest;
import com.campus.dto.TransactionRequest;
import com.campus.entity.SecondhandCategory;
import com.campus.entity.SecondhandProduct;
import com.campus.vo.SecondhandProductVO;
import com.campus.vo.SecondhandTransactionVO;

import java.util.List;

public interface SecondhandService {

    /** 获取所有分类（按sort排序） */
    List<SecondhandCategory> getCategories();

    /** 商品分页列表（公开，只查在售商品） */
    Page<SecondhandProductVO> getProductPage(Long categoryId, String keyword, int page, int size);

    /** 商品详情（浏览量+1） */
    SecondhandProductVO getProductDetail(Long productId, Long currentUserId);

    /** 发布商品 */
    void createProduct(Long userId, CreateProductRequest request);

    /** 编辑商品（只能编辑自己的） */
    void updateProduct(Long userId, Long productId, CreateProductRequest request);

    /** 修改商品状态（上架/下架/标记已售） */
    void updateProductStatus(Long userId, Long productId, Integer status);

    /** 删除商品（只能删除自己的） */
    void deleteProduct(Long userId, Long productId);

    /** 收藏/取消收藏 toggle */
    boolean toggleFavorite(Long userId, Long productId);

    /** 发起购买（创建交易记录） */
    void buyProduct(Long userId, TransactionRequest request);

    /** 我的发布 */
    Page<SecondhandProductVO> getMyProducts(Long userId, int page, int size);

    /** 我的收藏 */
    Page<SecondhandProductVO> getMyFavorites(Long userId, int page, int size);

    /** 我的交易记录 */
    Page<SecondhandTransactionVO> getMyTransactions(Long userId, int page, int size);

    /** 更新交易状态（交易双方可操作） */
    void updateTransactionStatus(Long userId, Long transactionId, Integer status);
}
