package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("forum_category")
public class ForumCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 类型 0学习论坛 1游戏论坛 */
    private Integer type;

    private String description;

    private String icon;

    private Integer sort;
}