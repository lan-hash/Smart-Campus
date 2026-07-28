package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.dto.CreateProductRequest;
import com.campus.dto.TransactionRequest;
import com.campus.entity.SecondhandCategory;
import com.campus.entity.SecondhandFavorite;
import com.campus.entity.SecondhandProduct;
import com.campus.entity.SecondhandTransaction;
import com.campus.mapper.SecondhandCategoryMapper;
import com.campus.mapper.SecondhandFavoriteMapper;
import com.campus.mapper.SecondhandProductMapper;
import com.campus.mapper.SecondhandTransactionMapper;
import com.campus.service.SecondhandService;
import com.campus.vo.SecondhandProductVO;
import com.campus.vo.SecondhandTransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecondhandServiceImpl implements SecondhandService {

    private final SecondhandCategoryMapper categoryMapper;
    private final SecondhandProductMapper productMapper;
    private final SecondhandFavoriteMapper favoriteMapper;
    private final SecondhandTransactionMapper transactionMapper;

    @Override
    public List<SecondhandCategory> getCategories() {
        return categoryMapper.selectAllOrdered();
    }

    @Override
    public Page<SecondhandProductVO> getProductPage(Long categoryId, String keyword, int page, int size) {
        Page<SecondhandProductVO> pageParam = new Page<>(page, size);
        return productMapper.selectProductPage(pageParam, categoryId, keyword);
    }

    @Override
    public SecondhandProductVO getProductDetail(Long productId, Long currentUserId) {
        SecondhandProductVO vo = productMapper.selectProductDetail(productId);
        if (vo == null) {
            throw new BusinessException("商品不存在");
        }
        // 浏览量 +1
        productMapper.update(null, new LambdaUpdateWrapper<SecondhandProduct>()
                .eq(SecondhandProduct::getId, productId)
                .setSql("view_count = view_count + 1"));
        vo.setViewCount(vo.getViewCount() + 1);
        // 当前用户是否收藏
        if (currentUserId != null) {
            vo.setIsFavorited(favoriteMapper.isFavorited(currentUserId, productId) > 0);
        }
        return vo;
    }

    @Override
    @Transactional
    public void createProduct(Long userId, CreateProductRequest request) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        SecondhandProduct product = new SecondhandProduct();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setConditionLevel(request.getConditionLevel());
        product.setImages(request.getImages());
        product.setLocation(request.getLocation());
        product.setContact(request.getContact());
        product.setStatus(0); // 默认在售
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);
    }

    @Override
    @Transactional
    public void updateProduct(Long userId, Long productId, CreateProductRequest request) {
        SecondhandProduct product = getProductAndCheckOwner(userId, productId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setConditionLevel(request.getConditionLevel());
        product.setImages(request.getImages());
        product.setLocation(request.getLocation());
        product.setContact(request.getContact());
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long userId, Long productId, Integer status) {
        SecondhandProduct product = getProductAndCheckOwner(userId, productId);
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException("无效的商品状态");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        getProductAndCheckOwner(userId, productId);
        productMapper.deleteById(productId);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long productId) {
        SecondhandProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        int count = favoriteMapper.isFavorited(userId, productId);
        if (count > 0) {
            // 已收藏 -> 取消收藏
            favoriteMapper.deleteFavorite(userId, productId);
            // 收藏数 -1
            productMapper.update(null, new LambdaUpdateWrapper<SecondhandProduct>()
                    .eq(SecondhandProduct::getId, productId)
                    .setSql("favorite_count = favorite_count - 1"));
            return false;
        } else {
            // 未收藏 -> 添加收藏
            favoriteMapper.insertFavorite(userId, productId);
            // 收藏数 +1
            productMapper.update(null, new LambdaUpdateWrapper<SecondhandProduct>()
                    .eq(SecondhandProduct::getId, productId)
                    .setSql("favorite_count = favorite_count + 1"));
            return true;
        }
    }

    @Override
    @Transactional
    public void buyProduct(Long userId, TransactionRequest request) {
        Long productId = request.getProductId();
        SecondhandProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 0) {
            throw new BusinessException("该商品当前不在售");
        }
        if (product.getUserId().equals(userId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        // 检查是否已有未完成交易
        Long pendingCount = transactionMapper.selectCount(new LambdaQueryWrapper<SecondhandTransaction>()
                .eq(SecondhandTransaction::getProductId, productId)
                .eq(SecondhandTransaction::getBuyerId, userId)
                .in(SecondhandTransaction::getStatus, 0, 1));
        if (pendingCount > 0) {
            throw new BusinessException("您已有该商品的进行中交易，请勿重复购买");
        }
        SecondhandTransaction transaction = new SecondhandTransaction();
        transaction.setProductId(productId);
        transaction.setBuyerId(userId);
        transaction.setSellerId(product.getUserId());
        transaction.setPrice(product.getPrice());
        transaction.setStatus(0); // 待确认
        transactionMapper.insert(transaction);
    }

    @Override
    public Page<SecondhandProductVO> getMyProducts(Long userId, int page, int size) {
        Page<SecondhandProductVO> pageParam = new Page<>(page, size);
        return productMapper.selectMyProducts(pageParam, userId);
    }

    @Override
    public Page<SecondhandProductVO> getMyFavorites(Long userId, int page, int size) {
        Page<SecondhandProductVO> pageParam = new Page<>(page, size);
        return productMapper.selectMyFavorites(pageParam, userId);
    }

    @Override
    public Page<SecondhandTransactionVO> getMyTransactions(Long userId, int page, int size) {
        Page<SecondhandTransactionVO> pageParam = new Page<>(page, size);
        return transactionMapper.selectMyTransactions(pageParam, userId);
    }

    @Override
    @Transactional
    public void updateTransactionStatus(Long userId, Long transactionId, Integer status) {
        SecondhandTransaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }
        // 只有交易双方可以操作
        if (!transaction.getBuyerId().equals(userId) && !transaction.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此交易");
        }
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("无效的交易状态");
        }
        // 状态流转校验
        Integer currentStatus = transaction.getStatus();
        if (currentStatus.equals(2) || currentStatus.equals(3)) {
            throw new BusinessException("交易已结束，无法修改状态");
        }
        transaction.setStatus(status);
        transactionMapper.updateById(transaction);
        // 如果交易取消，商品恢复在售
        if (status == 3) {
            productMapper.update(null, new LambdaUpdateWrapper<SecondhandProduct>()
                    .eq(SecondhandProduct::getId, transaction.getProductId())
                    .eq(SecondhandProduct::getStatus, 1)
                    .set(SecondhandProduct::getStatus, 0));
        }
        // 如果交易完成，商品标记已售
        if (status == 2) {
            productMapper.update(null, new LambdaUpdateWrapper<SecondhandProduct>()
                    .eq(SecondhandProduct::getId, transaction.getProductId())
                    .set(SecondhandProduct::getStatus, 1));
        }
    }

    /**
     * 获取商品并校验是否为当前用户发布
     */
    private SecondhandProduct getProductAndCheckOwner(Long userId, Long productId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        SecondhandProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己发布的商品");
        }
        return product;
    }
}
