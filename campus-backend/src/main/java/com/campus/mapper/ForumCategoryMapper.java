package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ForumCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ForumCategoryMapper extends BaseMapper<ForumCategory> {

    @Select("SELECT * FROM forum_category WHERE type = #{type} ORDER BY sort ASC")
    List<ForumCategory> selectByType(@Param("type") Integer type);
}