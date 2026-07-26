package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SysUser;
import com.campus.vo.UserVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(@Param("username") String username);

    @Insert("INSERT INTO user_follow (follower_id, following_id) VALUES (#{followerId}, #{followingId})")
    int insertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Insert("INSERT INTO user_block (user_id, blocked_id) VALUES (#{userId}, #{blockedId})")
    int insertBlock(@Param("userId") Long userId, @Param("blockedId") Long blockedId);

    @Delete("DELETE FROM user_block WHERE user_id = #{userId} AND blocked_id = #{blockedId}")
    int deleteBlock(@Param("userId") Long userId, @Param("blockedId") Long blockedId);

    List<UserVO> selectFollowingList(@Param("userId") Long userId);

    List<UserVO> selectFansList(@Param("userId") Long userId);
}