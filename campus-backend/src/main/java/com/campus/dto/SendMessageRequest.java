package com.campus.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SendMessageRequest {

    /** 接收者用户ID */
    @NotNull(message = "接收者不能为空")
    private Long toUserId;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 消息类型 0文字 1图片 */
    @NotNull(message = "消息类型不能为空")
    private Integer type;
}
