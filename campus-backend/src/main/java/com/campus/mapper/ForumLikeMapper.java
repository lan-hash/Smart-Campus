package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ForumLike;
import org.apache.ibatis.annotations.*;

public interface ForumLikeMapper extends BaseMapper<ForumLike> {

    @Select("SELECT COUNT(*) FROM forum_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int isLiked(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    @Insert("INSERT INTO forum_like (user_id, target_id, target_type) VALUES (#{userId}, #{targetId}, #{targetType})")
    int insertLike(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") Integer targetType);

    @Delete("DELETE FROM forum_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    int deleteLike(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") Integer targetType);
}