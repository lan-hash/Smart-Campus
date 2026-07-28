package com.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
