package com.schusterapps.orderservice.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "total is required")
    @DecimalMin(value = "0.01", message = "Total must be greater than zero")
    private BigDecimal total;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
