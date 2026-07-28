package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecondhandProductVO {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer conditionLevel;
    private String images;
    private String location;
    private String contact;
    private Integer status;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 发布者信息 */
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;

    /** 当前用户是否已收藏 */
    private Boolean isFavorited;
}
