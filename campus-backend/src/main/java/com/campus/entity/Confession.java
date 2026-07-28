package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("confession")
public class Confession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者ID */
    private Long userId;

    /** 表白内容 */
    private String content;

    /** 图片URL，逗号分隔 */
    private String images;

    /** 0实名 1匿名 */
    private Integer isAnonymous;

    private Integer likeCount;

    private Integer commentCount;

    /** 0正常 1审核 2违规 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
