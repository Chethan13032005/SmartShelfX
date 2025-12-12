package com.infosys.smartshelfx_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/po")
public class PurchaseOrderDraftController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class PoItem { public Long productId; public String sku; public String productName; public int quantity; }
    public static class PoDraft { public String vendorEmail; public String message; public List<PoItem> items = new ArrayList<>(); }

    @PostMapping("/draft")
    public PoDraft draftFromSuggestions(@RequestBody(required = false) Map<String, Object> body) {
        String sql = "SELECT p.id AS productId, p.sku, p.name AS productName, " +
                "GREATEST((fr.forecast_qty - p.current_stock), (p.reorder_level - p.current_stock)) AS qty " +
                "FROM products p LEFT JOIN (SELECT product_id, MAX(forecast_qty) AS forecast_qty FROM forecast_results GROUP BY product_id) fr " +
                "ON fr.product_id = p.id WHERE (p.current_stock < p.reorder_level) OR (fr.forecast_qty > p.current_stock)";

        List<PoItem> items = jdbcTemplate.query(sql, (rs, i) -> {
            PoItem it = new PoItem();
            it.productId = rs.getLong("productId");
            it.sku = rs.getString("sku");
            it.productName = rs.getString("productName");
            it.quantity = Math.max(0, rs.getInt("qty"));
            return it;
        });

        PoDraft draft = new PoDraft();
        draft.vendorEmail = (body != null && body.get("vendorEmail") != null) ? body.get("vendorEmail").toString() : "vendor@example.com";
        draft.items = items;
        draft.message = String.format("Dear Vendor,\n\nPlease provide the following items urgently due to forecasted demand and current stock levels:\n%s\n\nRegards,\nSmartShelfX",
                buildLines(items));
        return draft;
    }

    private String buildLines(List<PoItem> items) {
        StringBuilder sb = new StringBuilder();
        for (PoItem it : items) {
            sb.append(String.format("- %s (%s): %d units\n", it.productName, it.sku, it.quantity));
        }
        return sb.toString();
    }
}
