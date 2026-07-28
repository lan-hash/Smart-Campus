package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("confession_like")
public class ConfessionLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 表白ID */
    private Long confessionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
