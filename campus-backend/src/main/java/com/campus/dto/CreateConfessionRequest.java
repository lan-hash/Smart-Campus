package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateConfessionRequest {

    @NotBlank(message = "表白内容不能为空")
    @Size(max = 1000, message = "表白内容最多1000个字符")
    private String content;

    /** 图片URL，多个逗号分隔 */
    private String images;

    /** 0实名 1匿名，默认匿名 */
    private Integer isAnonymous;
}
