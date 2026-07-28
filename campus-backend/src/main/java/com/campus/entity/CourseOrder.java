package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_order")
public class CourseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者/需求方 */
    private Long userId;

    /** 接单者 */
    private Long acceptUserId;

    private String courseName;

    private String courseType;

    private String classTime;

    private String location;

    private BigDecimal salary;

    private String description;

    private String contact;

    /** 0待接单 1进行中 2已完成 3已取消 */
    private Integer status;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
