package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    private Long reporterId;

    /** 目标类型 0商品/1帖子/2表白/3代课/4用户 */
    private Integer targetType;

    /** 目标ID */
    private Long targetId;

    /** 举报原因 */
    private String reason;

    /** 详细描述 */
    private String description;

    /** 状态 0待处理/1已处理/2已驳回 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 处理时间 */
    private LocalDateTime handleTime;
}
