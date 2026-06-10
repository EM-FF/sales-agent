package org.com.salesagent.dto;

import java.math.BigDecimal;

public record RepSalesDTO(
        Long repId,
        String repName,
        Long regionId,
        String regionName,
        BigDecimal totalAmount,
        Integer orderCount
) {}
