package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.ForumPost;
import com.campus.vo.ForumPostVO;
import org.apache.ibatis.annotations.Param;

public interface ForumPostMapper extends BaseMapper<ForumPost> {

    Page<ForumPostVO> selectPostPage(Page<?> page, @Param("type") Integer type,
                                      @Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword);

    ForumPostVO selectPostDetail(@Param("postId") Long postId);

    Page<ForumPostVO> selectMyPosts(Page<?> page, @Param("userId") Long userId);

    Page<ForumPostVO> selectMyCollects(Page<?> page, @Param("userId") Long userId);
}