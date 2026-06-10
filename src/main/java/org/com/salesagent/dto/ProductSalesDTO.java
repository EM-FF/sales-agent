package org.com.salesagent.dto;

import java.math.BigDecimal;

public record ProductSalesDTO(
        Long productId,
        String skuCode,
        String productName,
        String category,
        BigDecimal totalAmount,
        Integer totalQuantity
) {}