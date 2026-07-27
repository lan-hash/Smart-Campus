package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ForumComment;
import com.campus.vo.ForumCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    List<ForumCommentVO> selectTopComments(@Param("postId") Long postId);

    List<ForumCommentVO> selectChildComments(@Param("parentIds") List<Long> parentIds);
}