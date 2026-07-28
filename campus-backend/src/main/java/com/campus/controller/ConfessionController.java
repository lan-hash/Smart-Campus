package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.CreateConfessionRequest;
import com.campus.service.ConfessionService;
import com.campus.vo.ConfessionCommentVO;
import com.campus.vo.ConfessionVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/confession")
@RequiredArgsConstructor
public class ConfessionController {

    private final ConfessionService confessionService;

    /** 分页获取表白列表（无需登录） */
    @GetMapping("/list")
    public Result<PageResult<ConfessionVO>> getList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ConfessionVO> result = confessionService.getConfessionPage(page, size, userId);
        return Result.success(PageResult.of(result));
    }

    /** 获取某条表白的评论列表（无需登录） */
    @GetMapping("/{id}/comments")
    public Result<List<ConfessionCommentVO>> getComments(@PathVariable Long id) {
        return Result.success(confessionService.getComments(id));
    }

    /** 发布表白（需登录） */
    @PostMapping
    public Result<Void> createConfession(HttpServletRequest request,
                                         @RequestBody @Valid CreateConfessionRequest createConfessionRequest) {
        Long userId = (Long) request.getAttribute("userId");
        confessionService.createConfession(userId, createConfessionRequest);
        return Result.success();
    }

    /** 点赞/取消点赞（需登录） */
    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = confessionService.toggleLike(userId, id);
        return Result.success(liked);
    }

    /** 发表评论（需登录） */
    @PostMapping("/{id}/comments")
    public Result<Void> addComment(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }
        confessionService.addComment(userId, id, content.trim());
        return Result.success();
    }

    /** 删除表白（需登录，只能删除自己的） */
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfession(HttpServletRequest request,
                                         @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        confessionService.deleteConfession(userId, id);
        return Result.success();
    }

    /** 获取当前用户发布的表白（需登录） */
    @GetMapping("/my")
    public Result<PageResult<ConfessionVO>> getMyConfessions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ConfessionVO> result = confessionService.getMyConfessions(userId, page, size);
        return Result.success(PageResult.of(result));
    }
}
