package com.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    /** user / assistant */
    private String role;

    private String content;

    private LocalDateTime createTime;
}
