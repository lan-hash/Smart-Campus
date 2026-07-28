package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseOrderVO {

    private Long id;
    private Long userId;
    private Long acceptUserId;
    private String courseName;
    private String courseType;
    private String classTime;
    private String location;
    private BigDecimal salary;
    private String description;
    private String contact;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 发布者昵称 */
    private String publisherNickname;
    /** 发布者头像 */
    private String publisherAvatar;
    /** 接单者昵称 */
    private String acceptorNickname;
    /** 接单者头像 */
    private String acceptorAvatar;
}
