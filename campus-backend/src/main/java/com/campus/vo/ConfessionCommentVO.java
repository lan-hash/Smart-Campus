package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfessionCommentVO {

    private Long id;
    private Long confessionId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;

    /** 评论者信息 */
    private String authorNickname;
    private String authorAvatar;
}
