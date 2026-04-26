package com.ims.controller;

import com.ims.dto.request.StockMovementRequest;
import com.ims.dto.response.ApiResponse;
import com.ims.dto.response.PageResponse;
import com.ims.dto.response.TransactionResponse;
import com.ims.entity.TransactionType;
import com.ims.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Record a stock-IN movement (purchase / receipt)")
    public ResponseEntity<ApiResponse<TransactionResponse>> stockIn(
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Stock-IN recorded", inventoryService.stockIn(request)));
    }

    @PostMapping("/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "Record a stock-OUT movement (sale / issue)")
    public ResponseEntity<ApiResponse<TransactionResponse>> stockOut(
            @Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Stock-OUT recorded", inventoryService.stockOut(request)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "List inventory transactions with optional filters")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> list(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                inventoryService.getAll(type, productId, from, to, pageable), t -> t)));
    }

    @GetMapping("/products/{productId}/transactions")
    @Operation(summary = "Transaction history for a single product")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> productHistory(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                inventoryService.getProductHistory(productId, pageable), t -> t)));
    }
}
