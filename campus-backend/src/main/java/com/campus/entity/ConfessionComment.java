package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("confession_comment")
public class ConfessionComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表白ID */
    private Long confessionId;

    /** 评论者ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
