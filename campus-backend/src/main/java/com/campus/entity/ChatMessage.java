package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户ID */
    private Long fromUserId;

    /** 接收者用户ID */
    private Long toUserId;

    /** 消息内容 */
    private String content;

    /** 消息类型 0文字 1图片 */
    private Integer type;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
