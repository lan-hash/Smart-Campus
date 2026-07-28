package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentReviewRequest {

    /** 目标类型：0帖子 1表白 2商品 3评论 */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    /** 目标ID */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /** 待审核内容 */
    @NotBlank(message = "审核内容不能为空")
    private String content;
}
