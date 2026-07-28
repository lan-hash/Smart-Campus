package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.SecondhandProduct;
import com.campus.vo.SecondhandProductVO;
import org.apache.ibatis.annotations.Param;

public interface SecondhandProductMapper extends BaseMapper<SecondhandProduct> {

    Page<SecondhandProductVO> selectProductPage(Page<?> page,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("keyword") String keyword);

    SecondhandProductVO selectProductDetail(@Param("productId") Long productId);

    Page<SecondhandProductVO> selectMyProducts(Page<?> page, @Param("userId") Long userId);

    Page<SecondhandProductVO> selectMyFavorites(Page<?> page, @Param("userId") Long userId);
}
