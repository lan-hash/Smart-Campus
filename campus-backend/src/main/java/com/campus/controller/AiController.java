package com.campus.controller;

import com.campus.common.result.Result;
import com.campus.dto.ChatRequest;
import com.campus.dto.ContentReviewRequest;
import com.campus.service.AiService;
import com.campus.vo.ChatMessageVO;
import com.campus.vo.ContentReviewVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 发送AI对话消息 */
    @PostMapping("/chat")
    public Result<ChatMessageVO> chat(HttpServletRequest request,
                                       @RequestBody @Valid ChatRequest chatRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.sendMessage(userId, chatRequest));
    }

    /** 获取用户会话列表 */
    @GetMapping("/chat/sessions")
    public Result<List<Map<String, Object>>> getSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.getSessions(userId));
    }

    /** 获取某会话的历史消息 */
    @GetMapping("/chat/{sessionId}/messages")
    public Result<List<ChatMessageVO>> getSessionMessages(HttpServletRequest request,
                                                            @PathVariable String sessionId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(aiService.getSessionMessages(userId, sessionId));
    }

    /** 删除会话 */
    @DeleteMapping("/chat/{sessionId}")
    public Result<Void> deleteSession(HttpServletRequest request,
                                      @PathVariable String sessionId) {
        Long userId = (Long) request.getAttribute("userId");
        aiService.deleteSession(userId, sessionId);
        return Result.success();
    }

    /** AI内容审核 */
    @PostMapping("/review")
    public Result<ContentReviewVO> reviewContent(@RequestBody @Valid ContentReviewRequest reviewRequest) {
        return Result.success(aiService.reviewContent(reviewRequest));
    }
}
