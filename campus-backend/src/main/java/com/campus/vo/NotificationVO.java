package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationVO {

    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 通知类型 0点赞 1评论 2关注 3系统 4交易 5代课 */
    private Integer type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联目标ID */
    private Long targetId;

    /** 触发通知的用户ID */
    private Long fromUserId;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    private LocalDateTime createTime;

    /** 触发通知的用户昵称 */
    private String fromUserNickname;

    /** 触发通知的用户头像 */
    private String fromUserAvatar;
}
