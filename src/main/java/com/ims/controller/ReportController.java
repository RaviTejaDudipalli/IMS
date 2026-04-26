package com.ims.controller;

import com.ims.dto.response.ApiResponse;
import com.ims.dto.response.InventorySummaryResponse;
import com.ims.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @Operation(summary = "High-level inventory summary + top selling products")
    public ResponseEntity<ApiResponse<InventorySummaryResponse>> summary(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int topN) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getSummary(days, topN)));
    }
}
