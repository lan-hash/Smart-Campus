package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论最多1000个字符")
    private String content;

    /** 父评论ID, 0或不传表示顶级评论 */
    private Long parentId;

    /** 回复目标用户ID */
    private Long replyToUserId;
}