package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_content_review")
public class AiContentReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 0帖子 1表白 2商品 3评论 */
    private Integer targetType;

    private Long targetId;

    private String content;

    /** pass / review / block */
    private String result;

    private String reason;

    /** 0无 1低 2中 3高 */
    private Integer riskLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
