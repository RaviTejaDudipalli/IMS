package com.ims.dto.response;

import com.ims.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private TransactionType type;
    private Integer quantity;
    private Integer quantityAfter;
    private String note;
    private String referenceCode;
    private String performedByUsername;
    private Instant timestamp;
}
