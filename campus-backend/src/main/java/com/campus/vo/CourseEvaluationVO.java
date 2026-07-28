package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseEvaluationVO {

    private Long id;
    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;

    /** 评价者昵称 */
    private String fromUserNickname;
    /** 评价者头像 */
    private String fromUserAvatar;
}
