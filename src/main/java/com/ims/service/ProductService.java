package com.ims.service;

import com.ims.dto.request.ProductRequest;
import com.ims.dto.response.ProductResponse;
import com.ims.entity.Category;
import com.ims.entity.Product;
import com.ims.entity.Supplier;
import com.ims.exception.DuplicateResourceException;
import com.ims.exception.ResourceNotFoundException;
import com.ims.mapper.ProductMapper;
import com.ims.repository.ProductRepository;
import com.ims.repository.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ProductMapper productMapper;

    @Value("${ims.inventory.default-low-stock-threshold:10}")
    private int defaultLowStockThreshold;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateResourceException("Product already exists with SKU: " + request.getSku());
        }
        Category category = categoryService.requireById(request.getCategoryId());
        Supplier supplier = supplierService.requireById(request.getSupplierId());

        int threshold = request.getLowStockThreshold() != null
                ? request.getLowStockThreshold()
                : defaultLowStockThreshold;

        Product product = Product.builder()
                .name(request.getName().trim())
                .sku(request.getSku().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .lowStockThreshold(threshold)
                .category(category)
                .supplier(supplier)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={} qty={}", saved.getId(), saved.getSku(), saved.getQuantity());
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = requireById(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateResourceException("Product already exists with SKU: " + request.getSku());
        }
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            product.setCategory(categoryService.requireById(request.getCategoryId()));
        }
        if (!product.getSupplier().getId().equals(request.getSupplierId())) {
            product.setSupplier(supplierService.requireById(request.getSupplierId()));
        }

        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        if (request.getLowStockThreshold() != null) {
            product.setLowStockThreshold(request.getLowStockThreshold());
        }
        log.info("Updated product id={} sku={}", id, product.getSku());
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(requireById(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String search,
                                        Long categoryId,
                                        Long supplierId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Boolean lowStockOnly,
                                        Boolean inStockOnly,
                                        Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.build(
                search, categoryId, supplierId, minPrice, maxPrice, lowStockOnly, inStockOnly);
        return productRepository.findAll(spec, pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStock() {
        return productRepository.findAllLowStock().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getLowStock(Pageable pageable) {
        return productRepository.findAllLowStock(pageable).map(productMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        Product product = requireById(id);
        productRepository.delete(product);
        log.info("Deleted product id={} sku={}", id, product.getSku());
    }

    Product requireById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }
}
