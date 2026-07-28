package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("secondhand_transaction")
public class SecondhandTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal price;

    /** 状态 0待确认 1交易中 2已完成 3已取消 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
