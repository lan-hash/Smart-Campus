package com.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "课程类型不能为空")
    private String courseType;

    @NotBlank(message = "上课时间不能为空")
    private String classTime;

    @NotBlank(message = "上课地点不能为空")
    private String location;

    @NotNull(message = "薪资不能为空")
    private BigDecimal salary;

    private String description;

    @NotBlank(message = "联系方式不能为空")
    private String contact;
}
