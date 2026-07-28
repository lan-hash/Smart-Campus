package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.SendMessageRequest;
import com.campus.entity.SystemNotice;
import com.campus.service.MessageService;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.NotificationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送私聊消息
     */
    @PostMapping("/chat")
    public Result<Void> sendMessage(HttpServletRequest request,
                                    @RequestBody @Valid SendMessageRequest sendMessageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.sendMessage(userId, sendMessageRequest);
        return Result.success();
    }

    /**
     * 获取与某用户的聊天记录（分页）
     */
    @GetMapping("/chat/{userId}")
    public Result<PageResult<ChatMessageVO>> getChatHistory(HttpServletRequest request,
                                                            @PathVariable Long userId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Page<ChatMessageVO> result = messageService.getChatHistory(currentUserId, userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /**
     * 获取当前用户的未读私聊总数
     */
    @GetMapping("/chat/unread")
    public Result<Integer> getUnreadChatCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = messageService.getUnreadChatCount(userId);
        return Result.success(count);
    }

    /**
     * 标记某用户发来的消息为已读
     */
    @PutMapping("/chat/read/{fromUserId}")
    public Result<Void> markChatAsRead(HttpServletRequest request,
                                       @PathVariable Long fromUserId) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markChatAsRead(userId, fromUserId);
        return Result.success();
    }

    /**
     * 上传图片并发送图片消息
     */
    @PostMapping("/chat/upload")
    public Result<Void> sendImageMessage(HttpServletRequest request,
                                         @RequestParam("toUserId") Long toUserId,
                                         @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.sendImageMessage(userId, toUserId, file);
        return Result.success();
    }

    /**
     * 获取当前用户的通知列表（分页）
     */
    @GetMapping("/notifications")
    public Result<PageResult<NotificationVO>> getNotifications(HttpServletRequest request,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<NotificationVO> result = messageService.getNotifications(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread")
    public Result<Integer> getUnreadNotificationCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = messageService.getUnreadNotificationCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/notifications/{id}/read")
    public Result<Void> markNotificationAsRead(HttpServletRequest request,
                                               @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markNotificationAsRead(userId, id);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllNotificationsAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markAllNotificationsAsRead(userId);
        return Result.success();
    }

    /**
     * 获取系统公告列表（分页，无需登录）
     */
    @GetMapping("/notices")
    public Result<PageResult<SystemNotice>> getSystemNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SystemNotice> result = messageService.getSystemNotices(page, size);
        return Result.success(PageResult.of(result));
    }
}
