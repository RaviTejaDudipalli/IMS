package com.ims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {
    private long totalProducts;
    private long totalCategories;
    private long totalSuppliers;
    private long lowStockCount;
    private long outOfStockCount;
    private BigDecimal totalInventoryValue;
    private long totalStockInTransactions;
    private long totalStockOutTransactions;
    private List<TopProductDto> topSellingProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductDto {
        private Long productId;
        private String name;
        private String sku;
        private long quantitySold;
    }
}
