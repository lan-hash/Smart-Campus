package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecondhandTransactionVO {

    private Long id;
    private Long productId;
    private String productTitle;
    private Long buyerId;
    private String buyerNickname;
    private String buyerAvatar;
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;
    private BigDecimal price;
    /** 状态 0待确认 1交易中 2已完成 3已取消 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
