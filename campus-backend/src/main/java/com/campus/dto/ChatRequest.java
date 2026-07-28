package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    /** 会话ID，为空时自动生成新会话 */
    private String sessionId;

    /** 用户消息 */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /** AI模型，可选，默认qwen */
    private String model;
}
