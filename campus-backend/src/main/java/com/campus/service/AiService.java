package com.campus.service;

import com.campus.dto.ChatRequest;
import com.campus.dto.ContentReviewRequest;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.ContentReviewVO;

import java.util.List;
import java.util.Map;

public interface AiService {

    /**
     * 发送AI对话消息
     *
     * @param userId  用户ID
     * @param request 聊天请求
     * @return AI回复的消息VO
     */
    ChatMessageVO sendMessage(Long userId, ChatRequest request);

    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表，包含sessionId、lastMessageTime、lastMessage
     */
    List<Map<String, Object>> getSessions(Long userId);

    /**
     * 获取某会话的历史消息
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessageVO> getSessionMessages(Long userId, String sessionId);

    /**
     * 删除会话
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     */
    void deleteSession(Long userId, String sessionId);

    /**
     * AI内容审核
     *
     * @param request 审核请求
     * @return 审核结果
     */
    ContentReviewVO reviewContent(ContentReviewRequest request);
}
