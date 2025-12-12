package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * GET /api/reports/inventory?format=xlsx|pdf|csv
     * Exports complete inventory report
     */
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> exportInventoryReport(
            @RequestParam(defaultValue = "xlsx") String format,
            Authentication authentication) {
        // PDF generation not yet implemented
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateInventoryReport(format);
            return buildFileResponse(reportData, "Inventory_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate inventory report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/stock-transactions?format=xlsx|pdf|csv&startDate=...&endDate=...
     * Exports stock transaction history
     */
    @GetMapping("/stock-transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> exportStockTransactionReport(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateStockTransactionReport(format, startDate, endDate);
            return buildFileResponse(reportData, "Stock_Transaction_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate stock transaction report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/purchase-orders?format=xlsx|pdf|csv&status=...
     * Exports purchase order report
     */
    @GetMapping("/purchase-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportPurchaseOrderReport(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generatePurchaseOrderReport(format, status);
            return buildFileResponse(reportData, "Purchase_Order_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate purchase order report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/low-stock?format=xlsx|pdf|csv
     * Exports low stock alert report
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> exportLowStockReport(
            @RequestParam(defaultValue = "xlsx") String format,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateLowStockReport(format);
            return buildFileResponse(reportData, "Low_Stock_Alert_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate low stock report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/ai-forecast?format=xlsx|pdf|csv&days=7|30
     * Exports AI forecasting report
     */
    @GetMapping("/ai-forecast")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> exportAIForecastReport(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateAIForecastReport(format, days);
            return buildFileResponse(reportData, "AI_Forecast_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate AI forecast report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/vendor-performance?format=xlsx|pdf|csv
     * Exports vendor performance report
     */
    @GetMapping("/vendor-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportVendorPerformanceReport(
            @RequestParam(defaultValue = "xlsx") String format,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateVendorPerformanceReport(format);
            return buildFileResponse(reportData, "Vendor_Performance_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate vendor performance report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/user-activity?format=xlsx|pdf|csv&startDate=...&endDate=...
     * Exports user activity audit log
     */
    @GetMapping("/user-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportUserActivityReport(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateUserActivityReport(format, startDate, endDate);
            return buildFileResponse(reportData, "User_Activity_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate user activity report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/notifications?format=xlsx|pdf|csv
     * Exports notifications history
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportNotificationsReport(
            @RequestParam(defaultValue = "xlsx") String format,
            Authentication authentication) {
        if ("pdf".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF export is not yet available. Please use XLSX or CSV format."));
        }
        try {
            byte[] reportData = reportService.generateNotificationsReport(format);
            return buildFileResponse(reportData, "Notifications_Report", format);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate notifications report: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reports/available
     * Returns list of available reports for the current user
     */
    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAvailableReports(Authentication authentication) {
        try {
                String roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .collect(Collectors.joining(","));

                Map<String, Object> response = new HashMap<>();
                response.put("reports", reportService.getAvailableReports(roles));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch available reports: " + e.getMessage()));
        }
    }

    /**
     * Helper method to build file download response
     */
    private ResponseEntity<Resource> buildFileResponse(byte[] data, String baseName, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = baseName + "_" + timestamp + "." + format.toLowerCase();

        MediaType mediaType;
        switch (format.toLowerCase()) {
            case "pdf":
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "csv":
                mediaType = MediaType.parseMediaType("text/csv");
                break;
            case "xlsx":
            default:
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
        }

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(data.length)
                .body(resource);
    }
}
