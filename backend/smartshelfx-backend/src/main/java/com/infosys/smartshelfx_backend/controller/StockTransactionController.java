package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.StockTransaction;
import com.infosys.smartshelfx_backend.service.StockTransactionService;
import com.infosys.smartshelfx_backend.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-transactions")
@CrossOrigin(origins = "*")
public class StockTransactionController {

    @Autowired
    private StockTransactionService stockTransactionService;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    // Record Stock-IN (Manager only)
    @PostMapping("/stock-in")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> recordStockIn(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = auth.getName();
            
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String notes = request.getOrDefault("notes", "").toString();
            
            StockTransaction transaction = stockTransactionService.recordStockIn(productId, quantity, notes, performedBy);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error recording stock-in: " + e.getMessage());
        }
    }

    // Record Stock-OUT (Manager only)
    @PostMapping("/stock-out")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> recordStockOut(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = auth.getName();
            
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String notes = request.getOrDefault("notes", "").toString();
            
            StockTransaction transaction = stockTransactionService.recordStockOut(productId, quantity, notes, performedBy);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error recording stock-out: " + e.getMessage());
        }
    }

    // Get all transactions (Admin and Manager)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllTransactions() {
        try {
            List<StockTransaction> transactions = stockTransactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching transactions: " + e.getMessage());
        }
    }

    // Get recent transactions for dashboard (Admin and Manager)
    @GetMapping("/recent")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRecentTransactions() {
        try {
            List<StockTransaction> recentTransactions = stockTransactionRepository.findTop10ByOrderByCreatedAtDesc();
            return ResponseEntity.ok(recentTransactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching recent transactions: " + e.getMessage());
        }
    }

    // Get transactions by product
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionsByProduct(@PathVariable Long productId) {
        try {
            List<StockTransaction> transactions = stockTransactionService.getTransactionsByProduct(productId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching transactions for product: " + e.getMessage());
        }
    }

    // Get transactions by type
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionsByType(@PathVariable String type) {
        try {
            List<StockTransaction> transactions = stockTransactionService.getTransactionsByType(type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching transactions by type: " + e.getMessage());
        }
    }

    // Get transaction by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        try {
            StockTransaction transaction = stockTransactionService.getTransactionById(id);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Transaction not found: " + e.getMessage());
        }
    }

    // Delete transaction (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        try {
            stockTransactionService.deleteTransaction(id);
            return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting transaction: " + e.getMessage());
        }
    }
}
