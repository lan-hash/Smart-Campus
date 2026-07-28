package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.CreateConfessionRequest;
import com.campus.vo.ConfessionCommentVO;
import com.campus.vo.ConfessionVO;

import java.util.List;

public interface ConfessionService {

    /** 分页获取表白列表 */
    Page<ConfessionVO> getConfessionPage(int page, int size, Long currentUserId);

    /** 获取表白评论列表 */
    List<ConfessionCommentVO> getComments(Long confessionId);

    /** 发布表白 */
    void createConfession(Long userId, CreateConfessionRequest request);

    /** 点赞/取消点赞 */
    boolean toggleLike(Long userId, Long confessionId);

    /** 发表评论 */
    void addComment(Long userId, Long confessionId, String content);

    /** 删除表白 */
    void deleteConfession(Long userId, Long confessionId);

    /** 获取当前用户发布的表白 */
    Page<ConfessionVO> getMyConfessions(Long userId, int page, int size);
}
