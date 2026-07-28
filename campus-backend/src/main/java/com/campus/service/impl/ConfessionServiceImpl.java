package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.dto.CreateConfessionRequest;
import com.campus.entity.Confession;
import com.campus.entity.ConfessionComment;
import com.campus.entity.ConfessionLike;
import com.campus.mapper.ConfessionCommentMapper;
import com.campus.mapper.ConfessionLikeMapper;
import com.campus.mapper.ConfessionMapper;
import com.campus.service.ConfessionService;
import com.campus.vo.ConfessionCommentVO;
import com.campus.vo.ConfessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfessionServiceImpl implements ConfessionService {

    private final ConfessionMapper confessionMapper;
    private final ConfessionCommentMapper commentMapper;
    private final ConfessionLikeMapper likeMapper;

    @Override
    public Page<ConfessionVO> getConfessionPage(int page, int size, Long currentUserId) {
        Page<ConfessionVO> pageParam = new Page<>(page, size);
        Page<ConfessionVO> result = confessionMapper.selectConfessionPage(pageParam);

        // 处理匿名信息
        processAnonymous(result.getRecords());

        // 如果用户已登录，查询点赞状态
        if (currentUserId != null && !result.getRecords().isEmpty()) {
            fillLikeStatus(result.getRecords(), currentUserId);
        }

        return result;
    }

    @Override
    public List<ConfessionCommentVO> getComments(Long confessionId) {
        Confession confession = confessionMapper.selectById(confessionId);
        if (confession == null) {
            throw new BusinessException("表白不存在");
        }
        return commentMapper.selectCommentsByConfessionId(confessionId);
    }

    @Override
    @Transactional
    public void createConfession(Long userId, CreateConfessionRequest request) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Confession confession = new Confession();
        confession.setUserId(userId);
        confession.setContent(request.getContent());
        confession.setImages(request.getImages());
        // 默认匿名
        confession.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : 1);
        confession.setLikeCount(0);
        confession.setCommentCount(0);
        confession.setStatus(0);
        confessionMapper.insert(confession);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long confessionId) {
        Confession confession = confessionMapper.selectById(confessionId);
        if (confession == null) {
            throw new BusinessException("表白不存在");
        }

        LambdaQueryWrapper<ConfessionLike> wrapper = new LambdaQueryWrapper<ConfessionLike>()
                .eq(ConfessionLike::getUserId, userId)
                .eq(ConfessionLike::getConfessionId, confessionId);
        ConfessionLike existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已点赞，取消点赞
            likeMapper.deleteById(existing.getId());
            confession.setLikeCount(Math.max(0, confession.getLikeCount() - 1));
            confessionMapper.updateById(confession);
            return false;
        } else {
            // 未点赞，添加点赞
            ConfessionLike like = new ConfessionLike();
            like.setUserId(userId);
            like.setConfessionId(confessionId);
            likeMapper.insert(like);
            confession.setLikeCount(confession.getLikeCount() + 1);
            confessionMapper.updateById(confession);
            return true;
        }
    }

    @Override
    @Transactional
    public void addComment(Long userId, Long confessionId, String content) {
        Confession confession = confessionMapper.selectById(confessionId);
        if (confession == null) {
            throw new BusinessException("表白不存在");
        }

        ConfessionComment comment = new ConfessionComment();
        comment.setConfessionId(confessionId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentMapper.insert(comment);

        // 更新表白评论数
        confession.setCommentCount(confession.getCommentCount() + 1);
        confessionMapper.updateById(confession);
    }

    @Override
    @Transactional
    public void deleteConfession(Long userId, Long confessionId) {
        Confession confession = confessionMapper.selectById(confessionId);
        if (confession == null) {
            throw new BusinessException("表白不存在");
        }
        if (!confession.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人表白");
        }
        confessionMapper.deleteById(confessionId);
    }

    @Override
    public Page<ConfessionVO> getMyConfessions(Long userId, int page, int size) {
        Page<ConfessionVO> pageParam = new Page<>(page, size);
        return confessionMapper.selectMyConfessionPage(pageParam, userId);
    }

    /**
     * 处理匿名表白：匿名时隐藏作者信息
     */
    private void processAnonymous(List<ConfessionVO> list) {
        for (ConfessionVO vo : list) {
            if (vo.getIsAnonymous() != null && vo.getIsAnonymous() == 1) {
                vo.setAuthorNickname("匿名用户");
                vo.setAuthorAvatar(null);
            }
        }
    }

    /**
     * 批量填充当前用户的点赞状态
     */
    private void fillLikeStatus(List<ConfessionVO> list, Long userId) {
        Set<Long> confessionIds = list.stream()
                .map(ConfessionVO::getId)
                .collect(Collectors.toSet());

        List<ConfessionLike> likes = likeMapper.selectList(
                new LambdaQueryWrapper<ConfessionLike>()
                        .eq(ConfessionLike::getUserId, userId)
                        .in(ConfessionLike::getConfessionId, confessionIds)
        );

        Set<Long> likedIds = likes.stream()
                .map(ConfessionLike::getConfessionId)
                .collect(Collectors.toSet());

        for (ConfessionVO vo : list) {
            vo.setIsLiked(likedIds.contains(vo.getId()));
        }
    }
}
