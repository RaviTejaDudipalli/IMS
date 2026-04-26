package com.ims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "inventory_transactions",
        indexes = {
                @Index(name = "idx_tx_product", columnList = "product_id"),
                @Index(name = "idx_tx_type", columnList = "type"),
                @Index(name = "idx_tx_timestamp", columnList = "timestamp")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"product", "performedBy"})
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tx_product"))
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(length = 500)
    private String note;

    @Column(length = 100)
    private String referenceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id",
            foreignKey = @ForeignKey(name = "fk_tx_user"))
    private User performedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}
