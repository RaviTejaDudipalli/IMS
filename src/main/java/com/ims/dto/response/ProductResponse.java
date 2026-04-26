package com.ims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private boolean lowStock;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private Instant createdAt;
    private Instant updatedAt;
}
