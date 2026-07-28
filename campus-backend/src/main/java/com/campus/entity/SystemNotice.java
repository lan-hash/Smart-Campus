package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_notice")
public class SystemNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布管理员ID */
    private Long adminId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
