package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.SecondhandFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SecondhandFavoriteMapper extends BaseMapper<SecondhandFavorite> {

    @Select("SELECT COUNT(*) FROM secondhand_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int isFavorited(@Param("userId") Long userId, @Param("productId") Long productId);

    @Insert("INSERT INTO secondhand_favorite (user_id, product_id) VALUES (#{userId}, #{productId})")
    int insertFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Delete("DELETE FROM secondhand_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteFavorite(@Param("userId") Long userId, @Param("productId") Long productId);
}
