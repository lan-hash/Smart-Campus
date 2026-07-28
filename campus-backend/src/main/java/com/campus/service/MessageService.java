package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.SendMessageRequest;
import com.campus.entity.SystemNotice;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.NotificationVO;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

    /**
     * 发送私聊消息
     */
    void sendMessage(Long userId, SendMessageRequest request);

    /**
     * 获取与某用户的聊天记录（分页）
     */
    Page<ChatMessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size);

    /**
     * 获取当前用户的未读私聊总数
     */
    int getUnreadChatCount(Long userId);

    /**
     * 标记某用户发来的消息为已读
     */
    void markChatAsRead(Long userId, Long fromUserId);

    /**
     * 获取当前用户的通知列表（分页）
     */
    Page<NotificationVO> getNotifications(Long userId, int page, int size);

    /**
     * 获取未读通知数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllNotificationsAsRead(Long userId);

    /**
     * 标记单条通知为已读
     */
    void markNotificationAsRead(Long userId, Long notificationId);

    /**
     * 获取系统公告列表（分页）
     */
    Page<SystemNotice> getSystemNotices(int page, int size);

    /**
     * 上传图片并发送图片消息
     */
    void sendImageMessage(Long userId, Long toUserId, MultipartFile file);
}
