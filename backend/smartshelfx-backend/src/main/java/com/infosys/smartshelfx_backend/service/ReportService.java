package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.*;
import com.infosys.smartshelfx_backend.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AIRestockService aiRestockService;

    /**
     * Generate Inventory Report
     */
    public byte[] generateInventoryReport(String format) throws Exception {
        List<Inventory> products = inventoryRepository.findAll();

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateInventoryExcel(products);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateInventoryCSV(products);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateInventoryPDF(products);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate Stock Transaction Report
     */
    public byte[] generateStockTransactionReport(String format, String startDate, String endDate) throws Exception {
        List<StockTransaction> transactions = stockTransactionRepository.findAll();

        // Filter by date if provided
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            transactions = transactions.stream()
                    .filter(t -> t.getCreatedAt().isAfter(start) || t.getCreatedAt().isEqual(start))
                    .collect(Collectors.toList());
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            transactions = transactions.stream()
                    .filter(t -> t.getCreatedAt().isBefore(end) || t.getCreatedAt().isEqual(end))
                    .collect(Collectors.toList());
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateStockTransactionExcel(transactions);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateStockTransactionCSV(transactions);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateStockTransactionPDF(transactions);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate Purchase Order Report
     */
    public byte[] generatePurchaseOrderReport(String format, String status) throws Exception {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            orders = orders.stream()
                    .filter(po -> status.equalsIgnoreCase(po.getStatus()))
                    .collect(Collectors.toList());
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            return generatePurchaseOrderExcel(orders);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generatePurchaseOrderCSV(orders);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generatePurchaseOrderPDF(orders);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate Low Stock Alert Report
     */
    public byte[] generateLowStockReport(String format) throws Exception {
        List<Inventory> lowStockProducts = inventoryRepository.findAll().stream()
                .filter(p -> p.getQuantity() <= (p.getReorderLevel() != null ? p.getReorderLevel() : 10))
                .collect(Collectors.toList());

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateLowStockExcel(lowStockProducts);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateLowStockCSV(lowStockProducts);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateLowStockPDF(lowStockProducts);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate AI Forecast Report
     */
    public byte[] generateAIForecastReport(String format, int days) throws Exception {
        List<Inventory> products = inventoryRepository.findAll();
        Map<Long, Double> forecastMap = new HashMap<>();

        // Get forecast data from forecast_results table
        if (tableExists("forecast_results")) {
            String sql = "SELECT product_id, MAX(forecast_qty) AS qty FROM forecast_results GROUP BY product_id";
            jdbcTemplate.query(sql, rs -> {
                forecastMap.put(rs.getLong("product_id"), rs.getDouble("qty"));
            });
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateAIForecastExcel(products, forecastMap, days);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateAIForecastCSV(products, forecastMap, days);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateAIForecastPDF(products, forecastMap, days);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate Vendor Performance Report
     */
    public byte[] generateVendorPerformanceReport(String format) throws Exception {
        List<User> vendors = userRepository.findByRole("Vendor");
        List<PurchaseOrder> allOrders = purchaseOrderRepository.findAll();

        Map<Long, VendorStats> vendorStatsMap = new HashMap<>();

        for (User vendor : vendors) {
            List<PurchaseOrder> vendorOrders = allOrders.stream()
                    .filter(po -> po.getVendorId() != null && po.getVendorId().equals(vendor.getId()))
                    .collect(Collectors.toList());

            VendorStats stats = new VendorStats();
            stats.vendorName = vendor.getFullName();
            stats.vendorEmail = vendor.getEmail();
            stats.totalOrders = vendorOrders.size();
            stats.approvedOrders = vendorOrders.stream().filter(po -> "APPROVED".equals(po.getStatus())).count();
            stats.completedOrders = vendorOrders.stream().filter(po -> "COMPLETED".equals(po.getStatus())).count();
            stats.pendingOrders = vendorOrders.stream().filter(po -> "PENDING".equals(po.getStatus())).count();

            // Calculate on-time delivery
            long onTimeDeliveries = vendorOrders.stream()
                    .filter(po -> po.getDeliveryDate() != null && "COMPLETED".equals(po.getStatus()))
                    .count();
            stats.onTimeDeliveryPercent = stats.completedOrders > 0 
                    ? (onTimeDeliveries * 100.0 / stats.completedOrders) 
                    : 0.0;

            vendorStatsMap.put(vendor.getId(), stats);
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateVendorPerformanceExcel(vendorStatsMap);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateVendorPerformanceCSV(vendorStatsMap);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateVendorPerformancePDF(vendorStatsMap);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate User Activity Report
     */
    public byte[] generateUserActivityReport(String format, String startDate, String endDate) throws Exception {
        // Get stock transactions as activity logs
        List<StockTransaction> activities = stockTransactionRepository.findAll();

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            activities = activities.stream()
                    .filter(a -> a.getCreatedAt().isAfter(start) || a.getCreatedAt().isEqual(start))
                    .collect(Collectors.toList());
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            activities = activities.stream()
                    .filter(a -> a.getCreatedAt().isBefore(end) || a.getCreatedAt().isEqual(end))
                    .collect(Collectors.toList());
        }

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateUserActivityExcel(activities);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateUserActivityCSV(activities);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateUserActivityPDF(activities);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Generate Notifications Report
     */
    public byte[] generateNotificationsReport(String format) throws Exception {
        List<Notification> notifications = notificationRepository.findAll();

        if ("xlsx".equalsIgnoreCase(format)) {
            return generateNotificationsExcel(notifications);
        } else if ("csv".equalsIgnoreCase(format)) {
            return generateNotificationsCSV(notifications);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return generateNotificationsPDF(notifications);
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    /**
     * Get available reports based on user role
     */
    public List<Map<String, Object>> getAvailableReports(String role) {
        String normalizedRole = role != null ? role.toUpperCase() : "";
        List<Map<String, Object>> reports = new ArrayList<>();

        if (normalizedRole.contains("ADMIN") || normalizedRole.contains("MANAGER")) {
            reports.add(createReportInfo("inventory", "Inventory Report", "Complete product catalog with stock levels"));
            reports.add(createReportInfo("stock-transactions", "Stock Transaction Report", "History of stock movements"));
            reports.add(createReportInfo("low-stock", "Low Stock Alert Report", "Products below reorder threshold"));
            reports.add(createReportInfo("ai-forecast", "AI Forecasting Report", "AI-powered demand predictions"));
        }

        if (normalizedRole.contains("ADMIN")) {
            reports.add(createReportInfo("purchase-orders", "Purchase Order Report", "Complete PO lifecycle tracking"));
            reports.add(createReportInfo("vendor-performance", "Vendor Performance Report", "Supplier evaluation metrics"));
            reports.add(createReportInfo("user-activity", "User Activity Report", "System audit log"));
            reports.add(createReportInfo("notifications", "Notifications Report", "Alert history"));
        }

        return reports;
    }

    // Excel generation methods
    private byte[] generateInventoryExcel(List<Inventory> products) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventory Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Product Name", "SKU", "Category", "Vendor", "Current Stock", "Reorder Level", "Reorder Quantity", "Price"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Inventory product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getName() != null ? product.getName() : product.getProductName());
                row.createCell(1).setCellValue(product.getSku());
                row.createCell(2).setCellValue(product.getCategory());
                row.createCell(3).setCellValue(product.getVendorEmail() != null ? product.getVendorEmail() : "");
                row.createCell(4).setCellValue(product.getQuantity());
                row.createCell(5).setCellValue(product.getReorderLevel() != null ? product.getReorderLevel() : 0);
                row.createCell(6).setCellValue(product.getReorderLevel() != null ? product.getReorderLevel() * 2 : 0);
                row.createCell(7).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateStockTransactionExcel(List<StockTransaction> transactions) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Stock Transactions");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Transaction ID", "Product", "Type", "Quantity", "Date & Time", "Handled By", "Notes"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (StockTransaction txn : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(txn.getId());
                
                Inventory product = inventoryRepository.findById(txn.getProductId()).orElse(null);
                row.createCell(1).setCellValue(product != null ? product.getName() : "Unknown");
                
                row.createCell(2).setCellValue(txn.getType());
                row.createCell(3).setCellValue(txn.getQuantity());
                row.createCell(4).setCellValue(txn.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.createCell(5).setCellValue(txn.getPerformedBy());
                row.createCell(6).setCellValue(txn.getNotes() != null ? txn.getNotes() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generatePurchaseOrderExcel(List<PurchaseOrder> orders) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Purchase Orders");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"PO Number", "Product", "Vendor", "Quantity", "Status", "Created By", "Approved By", "Delivery Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (PurchaseOrder po : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(po.getPoNumber());
                row.createCell(1).setCellValue(po.getProductName());
                row.createCell(2).setCellValue(po.getVendorEmail());
                row.createCell(3).setCellValue(po.getQuantity());
                row.createCell(4).setCellValue(po.getStatus());
                row.createCell(5).setCellValue(po.getCreatedBy());
                row.createCell(6).setCellValue(po.getApprovedBy() != null ? po.getApprovedBy() : "");
                row.createCell(7).setCellValue(po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateLowStockExcel(List<Inventory> products) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Low Stock Alert");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"SKU", "Product Name", "Current Stock", "Reorder Level", "Shortage", "Vendor"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Inventory product : products) {
                Row row = sheet.createRow(rowNum++);
                int reorderLevel = product.getReorderLevel() != null ? product.getReorderLevel() : 10;
                int shortage = reorderLevel - product.getQuantity();
                
                row.createCell(0).setCellValue(product.getSku());
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : product.getProductName());
                row.createCell(2).setCellValue(product.getQuantity());
                row.createCell(3).setCellValue(reorderLevel);
                row.createCell(4).setCellValue(shortage);
                row.createCell(5).setCellValue(product.getVendorEmail() != null ? product.getVendorEmail() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateAIForecastExcel(List<Inventory> products, Map<Long, Double> forecastMap, int days) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("AI Forecast");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"SKU", "Product", "Current Stock", "Predicted Demand (" + days + " days)", "Stock Deficit", "Restock Recommendation"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Inventory product : products) {
                Row row = sheet.createRow(rowNum++);
                Double forecast = forecastMap.getOrDefault(product.getId(), 0.0);
                int deficit = Math.max(0, (int) Math.round(forecast) - product.getQuantity());
                
                row.createCell(0).setCellValue(product.getSku());
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : product.getProductName());
                row.createCell(2).setCellValue(product.getQuantity());
                row.createCell(3).setCellValue(String.format("%.2f", forecast));
                row.createCell(4).setCellValue(deficit);
                row.createCell(5).setCellValue(deficit > 0 ? "Order " + deficit + " units" : "Sufficient stock");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateVendorPerformanceExcel(Map<Long, VendorStats> statsMap) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vendor Performance");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Vendor Name", "Email", "Total Orders", "Approved", "Completed", "Pending", "On-Time Delivery %"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (VendorStats stats : statsMap.values()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(stats.vendorName);
                row.createCell(1).setCellValue(stats.vendorEmail);
                row.createCell(2).setCellValue(stats.totalOrders);
                row.createCell(3).setCellValue(stats.approvedOrders);
                row.createCell(4).setCellValue(stats.completedOrders);
                row.createCell(5).setCellValue(stats.pendingOrders);
                row.createCell(6).setCellValue(String.format("%.1f%%", stats.onTimeDeliveryPercent));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateUserActivityExcel(List<StockTransaction> activities) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("User Activity");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Username", "Action", "Module", "Details", "Timestamp"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (StockTransaction activity : activities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(activity.getPerformedBy());
                row.createCell(1).setCellValue(activity.getType());
                row.createCell(2).setCellValue("Inventory Management");
                row.createCell(3).setCellValue("Product ID: " + activity.getProductId() + ", Quantity: " + activity.getQuantity());
                row.createCell(4).setCellValue(activity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateNotificationsExcel(List<Notification> notifications) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Notifications");

            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"User", "Type", "Message", "Priority", "Read", "Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Notification notif : notifications) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(notif.getUserEmail() != null ? notif.getUserEmail() : "All");
                row.createCell(1).setCellValue(notif.getType());
                row.createCell(2).setCellValue(notif.getMessage());
                row.createCell(3).setCellValue(notif.getPriority());
                row.createCell(4).setCellValue(notif.isRead() ? "Yes" : "No");
                row.createCell(5).setCellValue(notif.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // CSV generation methods (simplified)
    private byte[] generateInventoryCSV(List<Inventory> products) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("Product Name,SKU,Category,Vendor,Current Stock,Reorder Level,Reorder Quantity,Price\n");
        
        for (Inventory product : products) {
            csv.append(String.format("%s,%s,%s,%s,%d,%d,%d,%.2f\n",
                    escapeCsv(product.getName() != null ? product.getName() : product.getProductName()),
                    escapeCsv(product.getSku()),
                    escapeCsv(product.getCategory()),
                    escapeCsv(product.getVendorEmail() != null ? product.getVendorEmail() : ""),
                    product.getQuantity(),
                    product.getReorderLevel() != null ? product.getReorderLevel() : 0,
                    product.getReorderLevel() != null ? product.getReorderLevel() * 2 : 0,
                    product.getPrice() != null ? product.getPrice().doubleValue() : 0.0));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateStockTransactionCSV(List<StockTransaction> transactions) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("Transaction ID,Product,Type,Quantity,Date & Time,Handled By,Notes\n");
        
        for (StockTransaction txn : transactions) {
            Inventory product = inventoryRepository.findById(txn.getProductId()).orElse(null);
            csv.append(String.format("%d,%s,%s,%d,%s,%s,%s\n",
                    txn.getId(),
                    escapeCsv(product != null ? product.getName() : "Unknown"),
                    txn.getType(),
                    txn.getQuantity(),
                    txn.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    escapeCsv(txn.getPerformedBy()),
                    escapeCsv(txn.getNotes() != null ? txn.getNotes() : "")));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generatePurchaseOrderCSV(List<PurchaseOrder> orders) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("PO Number,Product,Vendor,Quantity,Status,Created By,Approved By,Delivery Date\n");
        
        for (PurchaseOrder po : orders) {
            csv.append(String.format("%s,%s,%s,%d,%s,%s,%s,%s\n",
                    escapeCsv(po.getPoNumber()),
                    escapeCsv(po.getProductName()),
                    escapeCsv(po.getVendorEmail()),
                    po.getQuantity(),
                    po.getStatus(),
                    escapeCsv(po.getCreatedBy()),
                    escapeCsv(po.getApprovedBy() != null ? po.getApprovedBy() : ""),
                    po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : ""));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateLowStockCSV(List<Inventory> products) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("SKU,Product Name,Current Stock,Reorder Level,Shortage,Vendor\n");
        
        for (Inventory product : products) {
            int reorderLevel = product.getReorderLevel() != null ? product.getReorderLevel() : 10;
            int shortage = reorderLevel - product.getQuantity();
            
            csv.append(String.format("%s,%s,%d,%d,%d,%s\n",
                    escapeCsv(product.getSku()),
                    escapeCsv(product.getName() != null ? product.getName() : product.getProductName()),
                    product.getQuantity(),
                    reorderLevel,
                    shortage,
                    escapeCsv(product.getVendorEmail() != null ? product.getVendorEmail() : "")));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateAIForecastCSV(List<Inventory> products, Map<Long, Double> forecastMap, int days) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append(String.format("SKU,Product,Current Stock,Predicted Demand (%d days),Stock Deficit,Restock Recommendation\n", days));
        
        for (Inventory product : products) {
            Double forecast = forecastMap.getOrDefault(product.getId(), 0.0);
            int deficit = Math.max(0, (int) Math.round(forecast) - product.getQuantity());
            
            csv.append(String.format("%s,%s,%d,%.2f,%d,%s\n",
                    escapeCsv(product.getSku()),
                    escapeCsv(product.getName() != null ? product.getName() : product.getProductName()),
                    product.getQuantity(),
                    forecast,
                    deficit,
                    escapeCsv(deficit > 0 ? "Order " + deficit + " units" : "Sufficient stock")));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateVendorPerformanceCSV(Map<Long, VendorStats> statsMap) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("Vendor Name,Email,Total Orders,Approved,Completed,Pending,On-Time Delivery %\n");
        
        for (VendorStats stats : statsMap.values()) {
            csv.append(String.format("%s,%s,%d,%d,%d,%d,%.1f%%\n",
                    escapeCsv(stats.vendorName),
                    escapeCsv(stats.vendorEmail),
                    stats.totalOrders,
                    stats.approvedOrders,
                    stats.completedOrders,
                    stats.pendingOrders,
                    stats.onTimeDeliveryPercent));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateUserActivityCSV(List<StockTransaction> activities) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("Username,Action,Module,Details,Timestamp\n");
        
        for (StockTransaction activity : activities) {
            csv.append(String.format("%s,%s,%s,%s,%s\n",
                    escapeCsv(activity.getPerformedBy()),
                    activity.getType(),
                    "Inventory Management",
                    escapeCsv("Product ID: " + activity.getProductId() + ", Quantity: " + activity.getQuantity()),
                    activity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        
        return csv.toString().getBytes();
    }

    private byte[] generateNotificationsCSV(List<Notification> notifications) throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("User,Type,Message,Priority,Read,Date\n");
        
        for (Notification notif : notifications) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s\n",
                    escapeCsv(notif.getUserEmail() != null ? notif.getUserEmail() : "All"),
                    notif.getType(),
                    escapeCsv(notif.getMessage()),
                    notif.getPriority(),
                    notif.isRead() ? "Yes" : "No",
                    notif.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        
        return csv.toString().getBytes();
    }

    // PDF generation methods (placeholder - return simple message)
    private byte[] generateInventoryPDF(List<Inventory> products) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateStockTransactionPDF(List<StockTransaction> transactions) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generatePurchaseOrderPDF(List<PurchaseOrder> orders) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateLowStockPDF(List<Inventory> products) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateAIForecastPDF(List<Inventory> products, Map<Long, Double> forecastMap, int days) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateVendorPerformancePDF(Map<Long, VendorStats> statsMap) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateUserActivityPDF(List<StockTransaction> activities) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    private byte[] generateNotificationsPDF(List<Notification> notifications) throws Exception {
        return "PDF generation not implemented yet. Please use XLSX or CSV format.".getBytes();
    }

    // Helper methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private Map<String, Object> createReportInfo(String id, String name, String description) {
        Map<String, Object> report = new HashMap<>();
        report.put("id", id);
        report.put("name", name);
        report.put("description", description);
        report.put("formats", Arrays.asList("xlsx", "csv", "pdf"));
        return report;
    }

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Inner class for vendor statistics
    static class VendorStats {
        String vendorName;
        String vendorEmail;
        int totalOrders;
        long approvedOrders;
        long completedOrders;
        long pendingOrders;
        double onTimeDeliveryPercent;
    }
}
