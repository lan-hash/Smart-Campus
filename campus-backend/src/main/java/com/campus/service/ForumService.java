package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.CreateCommentRequest;
import com.campus.dto.CreatePostRequest;
import com.campus.vo.ForumCategory;
import com.campus.vo.ForumCommentVO;
import com.campus.vo.ForumPostVO;

import java.util.List;

public interface ForumService {

    /** \u83b7\u53d6\u7248\u5757\u5217\u8868 */
    List<ForumCategory> getCategories(Integer type);

    /** \u5e16\u5b50\u5206\u9875\u5217\u8868 */
    Page<ForumPostVO> getPostPage(Integer type, Long categoryId, String keyword, int page, int size);

    /** \u5e16\u5b50\u8be6\u60c5 */
    ForumPostVO getPostDetail(Long postId);

    /** \u53d1\u5e16 */
    void createPost(Long userId, CreatePostRequest request);

    /** \u7f16\u8f91\u5e16\u5b50 */
    void updatePost(Long userId, Long postId, CreatePostRequest request);

    /** \u5220\u9664\u5e16\u5b50 */
    void deletePost(Long userId, Long postId);

    /** \u70b9\u8d5e/\u53d6\u6d88\u70b9\u8d5e */
    boolean toggleLike(Long userId, Long targetId, Integer targetType);

    /** \u6536\u85cf/\u53d6\u6d88\u6536\u85cf */
    boolean toggleCollect(Long userId, Long postId);

    /** \u83b7\u53d6\u8bc4\u8bba\u5217\u8868 */
    List<ForumCommentVO> getComments(Long postId, Long userId);

    /** \u53d1\u8868\u8bc4\u8bba */
    void addComment(Long userId, Long postId, CreateCommentRequest request);

    /** \u56de\u590d\u8bc4\u8bba */
    void replyComment(Long userId, Long commentId, CreateCommentRequest request);

    /** \u8bc4\u8bba\u70b9\u8d5e */
    boolean toggleCommentLike(Long userId, Long commentId);

    /** \u6211\u7684\u5e16\u5b50 */
    Page<ForumPostVO> getMyPosts(Long userId, int page, int size);

    /** \u6211\u7684\u6536\u85cf */
    Page<ForumPostVO> getMyCollects(Long userId, int page, int size);
}