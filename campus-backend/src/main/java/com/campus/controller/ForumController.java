package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.CreateCommentRequest;
import com.campus.dto.CreatePostRequest;
import com.campus.service.ForumService;
import com.campus.entity.ForumCategory;
import com.campus.vo.ForumCommentVO;
import com.campus.vo.ForumPostVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final ChatClient chatClient;

    /** 获取版块列表 */
    @GetMapping("/categories")
    public Result<List<ForumCategory>> getCategories(
            @RequestParam(required = false) Integer type) {
        return Result.success(forumService.getCategories(type));
    }

    /** 帖子分页列表 */
    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> getPosts(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ForumPostVO> result = forumService.getPostPage(type, categoryId, keyword, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 帖子详情 */
    @GetMapping("/posts/{postId}")
    public Result<ForumPostVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(forumService.getPostDetail(postId));
    }

    /** 发帖 */
    @PostMapping("/publish")
    public Result<Void> createPost(HttpServletRequest request,
                                   @RequestBody @Valid CreatePostRequest createPostRequest) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.createPost(userId, createPostRequest);
        return Result.success();
    }

    /** 编辑帖子 */
    @PutMapping("/posts/{postId}")
    public Result<Void> updatePost(HttpServletRequest request,
                                   @PathVariable Long postId,
                                   @RequestBody @Valid CreatePostRequest createPostRequest) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.updatePost(userId, postId, createPostRequest);
        return Result.success();
    }

    /** 删除帖子 */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(HttpServletRequest request,
                                   @PathVariable Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.deletePost(userId, postId);
        return Result.success();
    }

    /** 帖子点赞/取消点赞 */
    @PostMapping("/posts/{postId}/like")
    public Result<Boolean> toggleLike(HttpServletRequest request,
                                      @PathVariable Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = forumService.toggleLike(userId, postId, 0);
        return Result.success(liked);
    }

    /** 帖子收藏/取消收藏 */
    @PostMapping("/posts/{postId}/collect")
    public Result<Boolean> toggleCollect(HttpServletRequest request,
                                         @PathVariable Long postId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean collected = forumService.toggleCollect(userId, postId);
        return Result.success(collected);
    }

    /** 获取帖子评论列表 */
    @GetMapping("/posts/{postId}/comments")
    public Result<List<ForumCommentVO>> getComments(
            @PathVariable Long postId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(forumService.getComments(postId, userId));
    }

    /** 发表评论 */
    @PostMapping("/posts/{postId}/comments")
    public Result<Void> addComment(HttpServletRequest request,
                                   @PathVariable Long postId,
                                   @RequestBody @Valid CreateCommentRequest createCommentRequest) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.addComment(userId, postId, createCommentRequest);
        return Result.success();
    }

    /** 回复评论 */
    @PostMapping("/comments/{commentId}/reply")
    public Result<Void> replyComment(HttpServletRequest request,
                                     @PathVariable Long commentId,
                                     @RequestBody @Valid CreateCommentRequest createCommentRequest) {
        Long userId = (Long) request.getAttribute("userId");
        forumService.replyComment(userId, commentId, createCommentRequest);
        return Result.success();
    }

    /** 评论点赞/取消点赞 */
    @PostMapping("/comments/{commentId}/like")
    public Result<Boolean> toggleCommentLike(HttpServletRequest request,
                                              @PathVariable Long commentId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = forumService.toggleCommentLike(userId, commentId);
        return Result.success(liked);
    }

    /** 我的帖子 */
    @GetMapping("/my/posts")
    public Result<PageResult<ForumPostVO>> getMyPosts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ForumPostVO> result = forumService.getMyPosts(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 我的收藏 */
    @GetMapping("/my/collects")
    public Result<PageResult<ForumPostVO>> getMyCollects(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ForumPostVO> result = forumService.getMyCollects(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /** AI 智能分类（发帖时预览推荐版块） */
    @PostMapping("/ai/classify")
    public Result<String> aiClassify(@RequestBody java.util.Map<String, String> body) {
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) {
            return Result.success("");
        }
        // 查询所有版块名称供AI选择
        List<ForumCategory> categories = forumService.getCategories(null);
        StringBuilder catList = new StringBuilder();
        for (ForumCategory c : categories) {
            catList.append("- ").append(c.getName()).append("\n");
        }
        String prompt = "根据以下帖子标题和内容，从这些版块中选择最合适的一个，只回复版块名称，不要其他内容。\n"
                + "可选版块：\n" + catList + "\n"
                + "帖子标题：" + title + "\n"
                + "帖子内容：" + (content != null ? content.substring(0, Math.min(content.length(), 200)) : "");
        try {
            String reply = chatClient.prompt(prompt).call().content();
            return Result.success(reply != null ? reply.trim() : "");
        } catch (Exception e) {
            return Result.success("");
        }
    }
}