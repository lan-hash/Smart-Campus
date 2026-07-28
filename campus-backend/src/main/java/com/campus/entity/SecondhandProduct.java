package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("secondhand_product")
public class SecondhandProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    /** 新旧程度 1-10成新 */
    private Integer conditionLevel;

    /** 图片URL，多个用逗号分隔 */
    private String images;

    /** 交易地点 */
    private String location;

    /** 联系方式 */
    private String contact;

    /** 状态 0在售 1已售 2下架 */
    private Integer status;

    private Integer viewCount;

    private Integer favoriteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
