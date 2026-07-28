package com.campus.service.impl;

import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.config.OssConfig;
import com.campus.common.exception.BusinessException;
import com.campus.dto.SendMessageRequest;
import com.campus.entity.ChatMessage;
import com.campus.entity.Notification;
import com.campus.entity.SysUser;
import com.campus.entity.SystemNotice;
import com.campus.mapper.ChatMessageMapper;
import com.campus.mapper.NotificationMapper;
import com.campus.mapper.SystemNoticeMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.MessageService;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final NotificationMapper notificationMapper;
    private final SystemNoticeMapper systemNoticeMapper;
    private final UserMapper userMapper;
    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Override
    @Transactional
    public void sendMessage(Long userId, SendMessageRequest request) {
        // 校验接收者是否存在
        SysUser toUser = userMapper.selectById(request.getToUserId());
        if (toUser == null) {
            throw new BusinessException("接收者不存在");
        }
        if (userId.equals(request.getToUserId())) {
            throw new BusinessException("不能给自己发消息");
        }

        // 保存消息
        ChatMessage message = new ChatMessage();
        message.setFromUserId(userId);
        message.setToUserId(request.getToUserId());
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        // 创建一条通知提醒接收者
        SysUser fromUser = userMapper.selectById(userId);
        Notification notification = new Notification();
        notification.setUserId(request.getToUserId());
        notification.setType(3); // 系统通知
        notification.setTitle("新私信");
        notification.setContent((fromUser != null ? fromUser.getNickname() : "用户") + "给你发来一条消息");
        notification.setTargetId(message.getId());
        notification.setFromUserId(userId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public Page<ChatMessageVO> getChatHistory(Long userId, Long otherUserId, int page, int size) {
        // 查询全部聊天记录
        List<ChatMessageVO> allRecords = chatMessageMapper.selectChatHistory(userId, otherUserId);
        int total = allRecords.size();

        // 手动分页
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<ChatMessageVO> records;
        if (from >= total) {
            records = List.of();
        } else {
            records = allRecords.subList(from, to);
        }

        Page<ChatMessageVO> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    public int getUnreadChatCount(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }

    @Override
    public void markChatAsRead(Long userId, Long fromUserId) {
        chatMessageMapper.markAsRead(fromUserId, userId);
    }

    @Override
    public Page<NotificationVO> getNotifications(Long userId, int page, int size) {
        Page<NotificationVO> pageParam = new Page<>(page, size);
        return notificationMapper.selectNotificationPage(pageParam, userId);
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markAllNotificationsAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void markNotificationAsRead(Long userId, Long notificationId) {
        notificationMapper.markAsRead(userId, notificationId);
    }

    @Override
    public Page<SystemNotice> getSystemNotices(int page, int size) {
        Page<SystemNotice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemNotice::getCreateTime);
        return systemNoticeMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public void sendImageMessage(Long userId, Long toUserId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("图片不能为空");
        }

        // 校验接收者
        SysUser toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException("接收者不存在");
        }
        if (userId.equals(toUserId)) {
            throw new BusinessException("不能给自己发消息");
        }

        // 上传图片到 OSS
        String imageUrl = uploadImageToOss(file);

        // 发送图片消息
        SendMessageRequest request = new SendMessageRequest();
        request.setToUserId(toUserId);
        request.setContent(imageUrl);
        request.setType(1); // 图片类型
        sendMessage(userId, request);
    }

    /**
     * 上传图片到 OSS 并返回完整 URL
     */
    private String uploadImageToOss(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = FilenameUtils.getExtension(originalName);
            if (ext == null || ext.isEmpty()) {
                ext = "jpg";
            }

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = "campus/chat/" + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            }

            return ossConfig.getUrlPrefix() + objectName;
        } catch (Exception e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }
}
