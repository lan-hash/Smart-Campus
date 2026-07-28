package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.exception.BusinessException;
import com.campus.dto.ChatRequest;
import com.campus.dto.ContentReviewRequest;
import com.campus.entity.AiChatRecord;
import com.campus.entity.AiContentReview;
import com.campus.mapper.AiChatRecordMapper;
import com.campus.mapper.AiContentReviewMapper;
import com.campus.service.AiService;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.ContentReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiChatRecordMapper chatRecordMapper;
    private final AiContentReviewMapper contentReviewMapper;

    private final ChatClient chatClient;

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long userId, ChatRequest request) {
        // 生成sessionId
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // 1. 保存用户消息
        AiChatRecord userRecord = new AiChatRecord();
        userRecord.setUserId(userId);
        userRecord.setSessionId(sessionId);
        userRecord.setRole("user");
        userRecord.setContent(request.getMessage());
        chatRecordMapper.insert(userRecord);

        // 2. 调用AI获取回复
        String aiReply;
        try {
            aiReply = chatClient.prompt(request.getMessage()).call().content();
        } catch (Exception e) {
            throw new BusinessException("AI服务暂时不可用，请稍后重试");
        }

        // 3. 保存AI回复
        AiChatRecord assistantRecord = new AiChatRecord();
        assistantRecord.setUserId(userId);
        assistantRecord.setSessionId(sessionId);
        assistantRecord.setRole("assistant");
        assistantRecord.setContent(aiReply);
        chatRecordMapper.insert(assistantRecord);

        // 4. 返回AI回复VO
        ChatMessageVO vo = new ChatMessageVO();
        vo.setRole("assistant");
        vo.setContent(aiReply);
        vo.setCreateTime(assistantRecord.getCreateTime());
        return vo;
    }

    @Override
    public List<Map<String, Object>> getSessions(Long userId) {
        List<Map<String, Object>> sessions = chatRecordMapper.selectSessions(userId);
        // 补充每个会话的最后一条消息内容
        for (Map<String, Object> session : sessions) {
            String sessionId = (String) session.get("sessionId");
            String lastMessage = chatRecordMapper.selectLastMessage(userId, sessionId);
            // 截取前50个字符作为摘要
            if (lastMessage != null && lastMessage.length() > 50) {
                lastMessage = lastMessage.substring(0, 50) + "...";
            }
            session.put("lastMessage", lastMessage);
        }
        return sessions;
    }

    @Override
    public List<ChatMessageVO> getSessionMessages(Long userId, String sessionId) {
        List<AiChatRecord> records = chatRecordMapper.selectList(
                new LambdaQueryWrapper<AiChatRecord>()
                        .eq(AiChatRecord::getUserId, userId)
                        .eq(AiChatRecord::getSessionId, sessionId)
                        .orderByAsc(AiChatRecord::getCreateTime)
        );

        return records.stream().map(record -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setRole(record.getRole());
            vo.setContent(record.getContent());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        chatRecordMapper.deleteBySession(userId, sessionId);
    }

    @Override
    @Transactional
    public ContentReviewVO reviewContent(ContentReviewRequest request) {
        // 构建审核prompt
        String prompt = "请审核以下内容是否合规，只回复pass(合规)或block(违规)或review(需人工审核)，并给出理由："
                + request.getContent();

        String aiResponse;
        try {
            aiResponse = chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            throw new BusinessException("AI审核服务暂时不可用，请稍后重试");
        }

        // 解析AI返回结果
        String result;
        String reason;
        String responseLower = aiResponse.toLowerCase();

        if (responseLower.contains("block")) {
            result = "block";
        } else if (responseLower.contains("review")) {
            result = "review";
        } else {
            result = "pass";
        }

        // 提取理由：取冒号之后或换行之后的内容
        reason = aiResponse;
        if (aiResponse.contains("：")) {
            reason = aiResponse.substring(aiResponse.indexOf("：") + 1).trim();
        } else if (aiResponse.contains(":")) {
            reason = aiResponse.substring(aiResponse.indexOf(":") + 1).trim();
        }
        if (reason.length() > 500) {
            reason = reason.substring(0, 500);
        }

        // 根据result推断riskLevel
        Integer riskLevel;
        switch (result) {
            case "block":
                riskLevel = 3;
                break;
            case "review":
                riskLevel = 2;
                break;
            default:
                riskLevel = 0;
                break;
        }

        // 保存审核记录
        AiContentReview review = new AiContentReview();
        review.setTargetType(request.getTargetType());
        review.setTargetId(request.getTargetId());
        review.setContent(request.getContent());
        review.setResult(result);
        review.setReason(reason);
        review.setRiskLevel(riskLevel);
        contentReviewMapper.insert(review);

        // 返回VO
        ContentReviewVO vo = new ContentReviewVO();
        vo.setResult(result);
        vo.setReason(reason);
        vo.setRiskLevel(riskLevel);
        return vo;
    }
}
