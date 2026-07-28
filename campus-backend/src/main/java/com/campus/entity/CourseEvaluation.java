package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_evaluation")
public class CourseEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 */
    private Long orderId;

    /** 评价人 */
    private Long fromUserId;

    /** 被评价人 */
    private Long toUserId;

    /** 评分 1-5 */
    private Integer score;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
