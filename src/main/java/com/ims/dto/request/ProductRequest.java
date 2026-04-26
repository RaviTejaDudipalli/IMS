package com.ims.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 60)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "SKU may contain only letters, digits, '-' and '_'")
    private String sku;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be >= 0")
    private Integer quantity;

    @Min(value = 0, message = "Low stock threshold must be >= 0")
    private Integer lowStockThreshold;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "supplierId is required")
    private Long supplierId;
}
