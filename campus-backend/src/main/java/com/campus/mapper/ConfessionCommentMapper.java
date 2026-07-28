package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ConfessionComment;
import com.campus.vo.ConfessionCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfessionCommentMapper extends BaseMapper<ConfessionComment> {

    /** 查询某条表白的评论列表，LEFT JOIN sys_user获取评论者信息 */
    List<ConfessionCommentVO> selectCommentsByConfessionId(@Param("confessionId") Long confessionId);
}
