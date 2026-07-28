package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Confession;
import com.campus.vo.ConfessionVO;
import org.apache.ibatis.annotations.Param;

public interface ConfessionMapper extends BaseMapper<Confession> {

    /** 分页查询表白列表，LEFT JOIN sys_user获取作者信息 */
    Page<ConfessionVO> selectConfessionPage(Page<?> page);

    /** 查询当前用户发布的表白列表 */
    Page<ConfessionVO> selectMyConfessionPage(Page<?> page, @Param("userId") Long userId);
}
