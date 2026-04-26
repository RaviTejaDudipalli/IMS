package com.ims.service;

import com.ims.dto.response.InventorySummaryResponse;
import com.ims.entity.Product;
import com.ims.entity.TransactionType;
import com.ims.repository.CategoryRepository;
import com.ims.repository.InventoryTransactionRepository;
import com.ims.repository.ProductRepository;
import com.ims.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public InventorySummaryResponse getSummary(int days, int topN) {
        Instant to = Instant.now();
        Instant from = to.minus(days, ChronoUnit.DAYS);

        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalSuppliers = supplierRepository.count();

        List<Product> all = productRepository.findAll();
        long lowStock = all.stream().filter(Product::isLowStock).count();
        long outOfStock = all.stream().filter(p -> p.getQuantity() != null && p.getQuantity() == 0).count();

        BigDecimal totalValue = all.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long inCount = transactionRepository.countByTypeAndTimeRange(TransactionType.IN, from, to);
        long outCount = transactionRepository.countByTypeAndTimeRange(TransactionType.OUT, from, to);

        Map<Long, Product> byId = all.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<InventorySummaryResponse.TopProductDto> top = transactionRepository
                .findTopSellingProducts(from, to, PageRequest.of(0, Math.max(topN, 1)))
                .stream()
                .map(row -> {
                    Long productId = ((Number) row[0]).longValue();
                    long sold = ((Number) row[1]).longValue();
                    Product p = byId.get(productId);
                    return InventorySummaryResponse.TopProductDto.builder()
                            .productId(productId)
                            .name(p != null ? p.getName() : null)
                            .sku(p != null ? p.getSku() : null)
                            .quantitySold(sold)
                            .build();
                })
                .toList();

        return InventorySummaryResponse.builder()
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalSuppliers(totalSuppliers)
                .lowStockCount(lowStock)
                .outOfStockCount(outOfStock)
                .totalInventoryValue(totalValue)
                .totalStockInTransactions(inCount)
                .totalStockOutTransactions(outCount)
                .topSellingProducts(top)
                .build();
    }
}
