package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.dto.CreateCommentRequest;
import com.campus.dto.CreatePostRequest;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.service.ForumService;
import com.campus.vo.ForumCategory;
import com.campus.vo.ForumCommentVO;
import com.campus.vo.ForumPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumCategoryMapper categoryMapper;
    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final ForumLikeMapper likeMapper;
    private final ForumCollectMapper collectMapper;

    @Override
    public List<ForumCategory> getCategories(Integer type) {
        return categoryMapper.selectByType(type);
    }

    @Override
    public Page<ForumPostVO> getPostPage(Integer type, Long categoryId, String keyword, int page, int size) {
        Page<ForumPostVO> pageParam = new Page<>(page, size);
        return postMapper.selectPostPage(pageParam, type, categoryId, keyword);
    }

    @Override
    public ForumPostVO getPostDetail(Long postId) {
        ForumPostVO postVO = postMapper.selectPostDetail(postId);
        if (postVO == null) {
            throw new BusinessException("帖子不存在");
        }
        // 浏览量+1
        ForumPost post = new ForumPost();
        post.setId(postId);
        post.setViewCount(postVO.getViewCount() + 1);
        postMapper.updateById(post);
        postVO.setViewCount(postVO.getViewCount() + 1);
        return postVO;
    }

    @Override
    @Transactional
    public void createPost(Long userId, CreatePostRequest request) {
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(0);
        postMapper.insert(post);
    }

    @Override
    @Transactional
    public void updatePost(Long userId, Long postId, CreatePostRequest request) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权修改他人帖子");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());
        post.setCategoryId(request.getCategoryId());
        postMapper.updateById(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人帖子");
        }
        postMapper.deleteById(postId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long targetId, Integer targetType) {
        int count = likeMapper.isLiked(userId, targetId, targetType);
        if (count > 0) {
            // 已点赞, 取消点赞
            likeMapper.deleteLike(userId, targetId, targetType);
            if (targetType == 0) {
                // 帖子点赞数-1
                ForumPost post = postMapper.selectById(targetId);
                if (post != null) {
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                    postMapper.updateById(post);
                }
            } else {
                // 评论点赞数-1
                ForumComment comment = commentMapper.selectById(targetId);
                if (comment != null) {
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    commentMapper.updateById(comment);
                }
            }
            return false;
        } else {
            // 未点赞, 添加点赞
            likeMapper.insertLike(userId, targetId, targetType);
            if (targetType == 0) {
                ForumPost post = postMapper.selectById(targetId);
                if (post != null) {
                    post.setLikeCount(post.getLikeCount() + 1);
                    postMapper.updateById(post);
                }
            } else {
                ForumComment comment = commentMapper.selectById(targetId);
                if (comment != null) {
                    comment.setLikeCount(comment.getLikeCount() + 1);
                    commentMapper.updateById(comment);
                }
            }
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        int count = collectMapper.isCollected(userId, postId);
        if (count > 0) {
            collectMapper.deleteCollect(userId, postId);
            post.setCollectCount(Math.max(0, post.getCollectCount() - 1));
            postMapper.updateById(post);
            return false;
        } else {
            collectMapper.insertCollect(userId, postId);
            post.setCollectCount(post.getCollectCount() + 1);
            postMapper.updateById(post);
            return true;
        }
    }

    @Override
    public List<ForumCommentVO> getComments(Long postId, Long userId) {
        // 1. 查询所有顶级评论
        List<ForumCommentVO> topComments = commentMapper.selectTopComments(postId);

        if (topComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询子评论
        List<Long> parentIds = topComments.stream()
                .map(ForumCommentVO::getId)
                .collect(Collectors.toList());
        List<ForumCommentVO> allChildren = commentMapper.selectChildComments(parentIds);

        // 3. 如果用户已登录, 查询用户对这些评论的点赞状态
        if (userId != null) {
            List<Long> allCommentIds = new ArrayList<>(parentIds);
            allCommentIds.addAll(allChildren.stream().map(ForumCommentVO::getId).collect(Collectors.toList()));
            if (!allCommentIds.isEmpty()) {
                List<ForumLike> likes = likeMapper.selectList(
                        new LambdaQueryWrapper<ForumLike>()
                                .eq(ForumLike::getUserId, userId)
                                .eq(ForumLike::getTargetType, 1)
                                .in(ForumLike::getTargetId, allCommentIds)
                );
                Map<Long, Boolean> likedMap = likes.stream()
                        .collect(Collectors.toMap(ForumLike::getTargetId, l -> true));
                topComments.forEach(c -> c.setIsLiked(likedMap.getOrDefault(c.getId(), false)));
                allChildren.forEach(c -> c.setIsLiked(likedMap.getOrDefault(c.getId(), false)));
            }
        }

        // 4. 将子评论按 parentId 分组
        Map<Long, List<ForumCommentVO>> childrenMap = allChildren.stream()
                .collect(Collectors.groupingBy(ForumCommentVO::getParentId));

        // 5. 设置子评论到顶级评论
        topComments.forEach(comment -> {
            List<ForumCommentVO> children = childrenMap.getOrDefault(comment.getId(), new ArrayList<>());
            comment.setChildren(children.isEmpty() ? null : children);
        });

        return topComments;
    }

    @Override
    @Transactional
    public void addComment(Long userId, Long postId, CreateCommentRequest request) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 帖子评论数+1
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
    }

    @Override
    @Transactional
    public void replyComment(Long userId, Long commentId, CreateCommentRequest request) {
        ForumComment parentComment = commentMapper.selectById(commentId);
        if (parentComment == null) {
            throw new BusinessException("原评论不存在");
        }
        ForumComment comment = new ForumComment();
        comment.setPostId(parentComment.getPostId());
        comment.setUserId(userId);
        comment.setParentId(commentId);
        comment.setReplyToUserId(parentComment.getUserId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 帖子评论数+1
        ForumPost post = postMapper.selectById(parentComment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long userId, Long commentId) {
        return toggleLike(userId, commentId, 1);
    }

    @Override
    public Page<ForumPostVO> getMyPosts(Long userId, int page, int size) {
        Page<ForumPostVO> pageParam = new Page<>(page, size);
        return postMapper.selectMyPosts(pageParam, userId);
    }

    @Override
    public Page<ForumPostVO> getMyCollects(Long userId, int page, int size) {
        Page<ForumPostVO> pageParam = new Page<>(page, size);
        return postMapper.selectMyCollects(pageParam, userId);
    }
}
