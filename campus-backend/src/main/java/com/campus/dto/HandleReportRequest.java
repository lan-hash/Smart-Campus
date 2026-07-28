package com.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {

    /** 状态 1处理/2驳回 */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 处理备注 */
    private String handleRemark;
}
