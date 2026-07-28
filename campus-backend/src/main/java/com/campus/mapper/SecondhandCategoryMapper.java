package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.SecondhandCategory;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SecondhandCategoryMapper extends BaseMapper<SecondhandCategory> {

    @Select("SELECT * FROM secondhand_category ORDER BY sort ASC")
    List<SecondhandCategory> selectAllOrdered();
}
