package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfessionVO {

    private Long id;
    private Long userId;
    private String content;
    private String images;
    private Integer isAnonymous;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createTime;

    /** 作者信息 */
    private String authorNickname;
    private String authorAvatar;

    /** 当前用户是否点赞 */
    private Boolean isLiked;
}
