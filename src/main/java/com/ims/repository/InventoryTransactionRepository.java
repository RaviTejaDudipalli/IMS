package com.ims.repository;

import com.ims.entity.InventoryTransaction;
import com.ims.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);

    Page<InventoryTransaction> findByType(TransactionType type, Pageable pageable);

    Page<InventoryTransaction> findByProductIdAndType(Long productId,
                                                      TransactionType type,
                                                      Pageable pageable);

    Page<InventoryTransaction> findByTimestampBetween(Instant from, Instant to, Pageable pageable);

    @Query("SELECT t.product.id AS productId, " +
            "SUM(CASE WHEN t.type = com.ims.entity.TransactionType.OUT THEN t.quantity ELSE 0 END) AS soldQty " +
            "FROM InventoryTransaction t " +
            "WHERE t.timestamp BETWEEN :from AND :to " +
            "GROUP BY t.product.id " +
            "ORDER BY soldQty DESC")
    List<Object[]> findTopSellingProducts(@Param("from") Instant from,
                                          @Param("to") Instant to,
                                          Pageable pageable);

    @Query("SELECT COUNT(t) FROM InventoryTransaction t " +
            "WHERE t.type = :type AND t.timestamp BETWEEN :from AND :to")
    long countByTypeAndTimeRange(@Param("type") TransactionType type,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);
}
