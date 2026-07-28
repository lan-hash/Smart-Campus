package com.campus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 500, min = 1,message = "商品标题不能超过100个字符")
    private String title;

    private String description;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "新旧程度不能为空")
    private Integer conditionLevel;

    private String images;

    private String location;

    private String contact;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
}
