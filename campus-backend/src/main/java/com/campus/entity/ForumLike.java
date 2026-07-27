package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_like")
public class ForumLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 目标ID */
    private Long targetId;

    /** 类型 0帖子 1评论 */
    private Integer targetType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}