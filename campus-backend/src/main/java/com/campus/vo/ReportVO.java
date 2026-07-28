package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportVO {

    private Long id;
    private Long reporterId;
    private Integer targetType;
    private Long targetId;
    private String reason;
    private String description;
    private Integer status;
    private Long handlerId;
    private String handleRemark;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;

    /** 举报人昵称 */
    private String reporterNickname;

    /** 举报人头像 */
    private String reporterAvatar;

    /** 目标信息（标题或名称） */
    private String targetTitle;

    /** 处理人昵称 */
    private String handlerNickname;
}
