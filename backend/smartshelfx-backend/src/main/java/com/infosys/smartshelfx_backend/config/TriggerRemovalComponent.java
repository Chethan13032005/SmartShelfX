package com.infosys.smartshelfx_backend.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * CRITICAL: Removes database triggers that block stock transactions
 * This runs BEFORE DataInitializer to ensure triggers are removed first
 */
@Component
@Order(1) // Run first, before DataInitializer
public class TriggerRemovalComponent {

    private final DataSource dataSource;

    public TriggerRemovalComponent(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void removeTriggers() {
        System.out.println("\n🔧🔧🔧 CRITICAL: Checking and removing database triggers...");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // First, show what triggers exist
            System.out.println("📋 Current triggers on stock_transactions:");
            try (ResultSet rs = stmt.executeQuery("SHOW TRIGGERS WHERE `Table` = 'stock_transactions'")) {
                boolean hasTriggers = false;
                while (rs.next()) {
                    hasTriggers = true;
                    String triggerName = rs.getString("Trigger");
                    String timing = rs.getString("Timing");
                    String event = rs.getString("Event");
                    System.out.println("  ⚠️  Found trigger: " + triggerName + " (" + timing + " " + event + ")");
                }
                if (!hasTriggers) {
                    System.out.println("  ✅ No triggers found - Good!");
                }
            }
            
            // Drop all possible trigger names
            String[] triggerNames = {
                "check_stock_before_transaction",
                "validate_stock_transaction",
                "check_stock_availability",
                "stock_validation_trigger",
                "prevent_negative_stock",
                "stock_check_trigger",
                "before_stock_transaction_insert",
                "before_insert_stock_transactions",
                "stock_transactions_before_insert",
                "trg_check_stock_before_transaction",
                "trg_validate_stock",
                "stock_transactions_BEFORE_INSERT",
                "check_insufficient_stock",
                "validate_stock_before_insert"
            };
            
            for (String triggerName : triggerNames) {
                try {
                    stmt.execute("DROP TRIGGER IF EXISTS " + triggerName);
                    System.out.println("  ✅ Attempted to drop: " + triggerName);
                } catch (Exception e) {
                    System.out.println("  ℹ️  Could not drop " + triggerName + ": " + e.getMessage());
                }
            }
            
            // Verify triggers are removed
            System.out.println("\n🔍 Verifying triggers after removal:");
            try (ResultSet rs = stmt.executeQuery("SHOW TRIGGERS WHERE `Table` = 'stock_transactions'")) {
                boolean stillHasTriggers = false;
                while (rs.next()) {
                    stillHasTriggers = true;
                    String triggerName = rs.getString("Trigger");
                    System.out.println("  🚨 ERROR: Trigger STILL EXISTS: " + triggerName);
                    System.out.println("  📝 SQL to remove it manually:");
                    System.out.println("     DROP TRIGGER IF EXISTS " + triggerName + ";");
                }
                
                if (stillHasTriggers) {
                    System.out.println("\n❌❌❌ CRITICAL ERROR: Database triggers are STILL ACTIVE!");
                    System.out.println("⚠️  Stock-OUT operations will FAIL until these triggers are removed!");
                    System.out.println("📋 Please run the SQL commands shown above in MySQL Workbench or command line.");
                } else {
                    System.out.println("  ✅✅✅ SUCCESS! All triggers removed!");
                    System.out.println("  🎉 Stock-OUT operations should now work correctly!");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR removing triggers: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🔧🔧🔧 Trigger removal check complete!\n");
    }
}
