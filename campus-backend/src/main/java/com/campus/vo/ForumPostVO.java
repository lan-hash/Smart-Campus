package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForumPostVO {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String images;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer isTop;
    private Integer isEssence;
    private Integer status;
    private String aiCategory;
    private LocalDateTime createTime;

    /** 发布者信息 */
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;

    /** 当前用户是否点赞/收藏 */
    private Boolean isLiked;
    private Boolean isCollected;
}