package com.ims.service;

import com.ims.dto.request.StockMovementRequest;
import com.ims.dto.response.TransactionResponse;
import com.ims.entity.InventoryTransaction;
import com.ims.entity.Product;
import com.ims.entity.TransactionType;
import com.ims.entity.User;
import com.ims.exception.InsufficientStockException;
import com.ims.exception.ResourceNotFoundException;
import com.ims.mapper.TransactionMapper;
import com.ims.repository.InventoryTransactionRepository;
import com.ims.repository.ProductRepository;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse stockIn(StockMovementRequest request) {
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", request.getProductId()));

        int newQty = product.getQuantity() + request.getQuantity();
        product.setQuantity(newQty);

        InventoryTransaction tx = saveTransaction(product, TransactionType.IN, request, newQty);
        log.info("STOCK IN sku={} qty={} newQty={} ref={}",
                product.getSku(), request.getQuantity(), newQty, request.getReferenceCode());
        return transactionMapper.toResponse(tx);
    }

    @Transactional
    public TransactionResponse stockOut(StockMovementRequest request) {
        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw InsufficientStockException.forProduct(
                    product.getSku(), request.getQuantity(), product.getQuantity());
        }
        int newQty = product.getQuantity() - request.getQuantity();
        product.setQuantity(newQty);

        InventoryTransaction tx = saveTransaction(product, TransactionType.OUT, request, newQty);
        log.info("STOCK OUT sku={} qty={} newQty={} ref={}",
                product.getSku(), request.getQuantity(), newQty, request.getReferenceCode());
        if (product.isLowStock()) {
            log.warn("LOW STOCK ALERT sku={} qty={} threshold={}",
                    product.getSku(), product.getQuantity(), product.getLowStockThreshold());
        }
        return transactionMapper.toResponse(tx);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getProductHistory(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException.of("Product", productId);
        }
        return transactionRepository.findByProductId(productId, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(TransactionType type,
                                            Long productId,
                                            Instant from,
                                            Instant to,
                                            Pageable pageable) {
        Page<InventoryTransaction> page;
        if (productId != null && type != null) {
            page = transactionRepository.findByProductIdAndType(productId, type, pageable);
        } else if (productId != null) {
            page = transactionRepository.findByProductId(productId, pageable);
        } else if (type != null) {
            page = transactionRepository.findByType(type, pageable);
        } else if (from != null && to != null) {
            page = transactionRepository.findByTimestampBetween(from, to, pageable);
        } else {
            page = transactionRepository.findAll(pageable);
        }
        return page.map(transactionMapper::toResponse);
    }

    private InventoryTransaction saveTransaction(Product product,
                                                 TransactionType type,
                                                 StockMovementRequest request,
                                                 int quantityAfter) {
        User performedBy = currentUser();
        InventoryTransaction tx = InventoryTransaction.builder()
                .product(product)
                .type(type)
                .quantity(request.getQuantity())
                .quantityAfter(quantityAfter)
                .note(request.getNote())
                .referenceCode(request.getReferenceCode())
                .performedBy(performedBy)
                .build();
        return transactionRepository.save(tx);
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
