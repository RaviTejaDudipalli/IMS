package com.ims.mapper;

import com.ims.dto.response.TransactionResponse;
import com.ims.entity.InventoryTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(InventoryTransaction tx) {
        if (tx == null) return null;
        return TransactionResponse.builder()
                .id(tx.getId())
                .productId(tx.getProduct() != null ? tx.getProduct().getId() : null)
                .productSku(tx.getProduct() != null ? tx.getProduct().getSku() : null)
                .productName(tx.getProduct() != null ? tx.getProduct().getName() : null)
                .type(tx.getType())
                .quantity(tx.getQuantity())
                .quantityAfter(tx.getQuantityAfter())
                .note(tx.getNote())
                .referenceCode(tx.getReferenceCode())
                .performedByUsername(tx.getPerformedBy() != null ? tx.getPerformedBy().getUsername() : null)
                .timestamp(tx.getTimestamp())
                .build();
    }
}
