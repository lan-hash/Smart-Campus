package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForumCommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;

    /** 评论者信息 */
    private String authorNickname;
    private String authorAvatar;

    /** 回复目标用户昵称 */
    private String replyToNickname;

    /** 当前用户是否点赞 */
    private Boolean isLiked;

    /** 子评论列表 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ForumCommentVO> children;
}