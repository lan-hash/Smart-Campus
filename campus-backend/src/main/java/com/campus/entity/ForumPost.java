package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String content;

    /** 图片URL，多个用逗号分隔 */
    private String images;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    /** 0普通 1置顶 */
    private Integer isTop;

    /** 0普通 1加精 */
    private Integer isEssence;

    /** 0正常 1审核中 2违规 */
    private Integer status;

    /** AI智能分类标签 */
    private String aiCategory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}