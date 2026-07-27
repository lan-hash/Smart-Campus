package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull(message = "版块ID不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 150, message = "标题最多150个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** 图片URL, 多个逗号分隔 */
    private String images;

    /** 标签ID, 多个逗号分隔 */
    private String tagIds;
}