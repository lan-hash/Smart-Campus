package com.campus.vo;

import lombok.Data;

@Data
public class ContentReviewVO {

    /** pass / review / block */
    private String result;

    private String reason;

    /** 0无 1低 2中 3高 */
    private Integer riskLevel;
}
