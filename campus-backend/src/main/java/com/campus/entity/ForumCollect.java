package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_collect")
public class ForumCollect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}