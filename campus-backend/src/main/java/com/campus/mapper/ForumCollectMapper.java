package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ForumCollect;
import org.apache.ibatis.annotations.*;

public interface ForumCollectMapper extends BaseMapper<ForumCollect> {

    @Select("SELECT COUNT(*) FROM forum_collect WHERE user_id = #{userId} AND post_id = #{postId}")
    int isCollected(@Param("userId") Long userId, @Param("postId") Long postId);

    @Insert("INSERT INTO forum_collect (user_id, post_id) VALUES (#{userId}, #{postId})")
    int insertCollect(@Param("userId") Long userId, @Param("postId") Long postId);

    @Delete("DELETE FROM forum_collect WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteCollect(@Param("userId") Long userId, @Param("postId") Long postId);
}