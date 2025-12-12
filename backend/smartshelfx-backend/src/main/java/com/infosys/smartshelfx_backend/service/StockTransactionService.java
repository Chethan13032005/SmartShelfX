package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.model.StockTransaction;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import com.infosys.smartshelfx_backend.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockTransactionService {

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private NotificationService notificationService;

    // Record Stock-IN transaction
    @Transactional
    public StockTransaction recordStockIn(Long productId, Integer quantity, String notes, String performedBy) {
        System.out.println("📦 Stock-IN Request - Product ID: " + productId + ", Quantity to add: " + quantity + ", Performed by: " + performedBy);
        
        // 1️⃣ Step 1: Fetch product from database
        Inventory product = inventoryRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("❌ Product not found with ID: " + productId));
        
        System.out.println("✅ Product found: " + product.getName());
        
        // 2️⃣ Step 2: Get current stock
        int currentStock = product.getStock();
        System.out.println("📊 Current stock: " + currentStock);
        
        // 3️⃣ Step 3: Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("❌ Invalid quantity for Stock-IN. Quantity must be greater than 0.");
        }
        
        // 4️⃣ Step 4: Calculate new stock and update inventory
        int newStock = currentStock + quantity;
        product.setStock(newStock);
        inventoryRepository.save(product);
        inventoryRepository.flush(); // Force immediate database update
        
        System.out.println("✅ Inventory updated - Previous stock: " + currentStock + " → New stock: " + newStock);
        
        // 5️⃣ Step 5: Create and save transaction record
        StockTransaction transaction = new StockTransaction();
        transaction.setProductId(productId);
        transaction.setType("IN");
        transaction.setQuantity(quantity);
        transaction.setNotes(notes);
        transaction.setPerformedBy(performedBy);
        
        StockTransaction saved = stockTransactionRepository.save(transaction);
        System.out.println("✅ Stock-IN transaction recorded with ID: " + saved.getId());
        
        // 6️⃣ Step 6: Create notification
        String message = String.format("📥 Stock-IN: %d units of '%s' added by %s. Previous stock: %d → New stock: %d", 
                                      quantity, product.getName(), performedBy, currentStock, newStock);
        notificationService.createNotification("SYSTEM", message, null, "Manager", 
                                              "NORMAL", "STOCK_TRANSACTION", saved.getId());
        
        System.out.println("✅ Stock-IN Success - Transaction ID: " + saved.getId());
        return saved;
    }

    // Record Stock-OUT transaction
    @Transactional
    public StockTransaction recordStockOut(Long productId, Integer quantity, String notes, String performedBy) {
        // 1️⃣ Fetch product from database
        Inventory product = inventoryRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // 2️⃣ Get current available stock
        int availableStock = product.getStock(); // Total current stock in database
        
        System.out.println("📦 Stock-OUT Request - Product: " + product.getName() + 
                          ", Available Stock: " + availableStock + 
                          ", Requested Quantity: " + quantity);

        // 3️⃣ Validate stock availability
        if (availableStock <= 0) {
            throw new RuntimeException("No stock available for " + product.getName() + 
                                     ". Current stock: " + availableStock + ". Please Stock-IN first.");
        }

        if (quantity > availableStock) {
            throw new RuntimeException("Insufficient stock for " + product.getName() + 
                                     ". Only " + availableStock + " units available, but " + 
                                     quantity + " units requested.");
        }

        // 4️⃣ Deduct the requested quantity from total stock
        int newStock = availableStock - quantity;
        product.setStock(newStock);
        inventoryRepository.save(product);
        inventoryRepository.flush(); // Force immediate database update

        // 5️⃣ Record the Stock-OUT transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProductId(productId);
        transaction.setType("OUT");
        transaction.setQuantity(quantity);
        transaction.setNotes(notes != null && !notes.trim().isEmpty() ? notes : "Stock-OUT operation successful");
        transaction.setPerformedBy(performedBy);

        // Save transaction record
        StockTransaction saved = stockTransactionRepository.save(transaction);

        System.out.println("✅ Stock-OUT Success - Product: " + product.getName() + 
                          ", Previous Stock: " + availableStock + 
                          ", New Stock: " + newStock);

        // 6️⃣ Create notification
        String message = String.format("📤 Stock-OUT: %d units of '%s' removed by %s. Previous stock: %d → New stock: %d", 
                                      quantity, product.getName(), performedBy, availableStock, newStock);
        notificationService.createNotification("SYSTEM", message, null, "Manager", 
                                              "NORMAL", "STOCK_TRANSACTION", saved.getId());

        // 7️⃣ Check if stock falls below reorder level
        if (newStock <= product.getReorderLevel()) {
            notificationService.createLowStockAlert(productId, product.getName(), newStock, product.getReorderLevel());
        }

        return saved;
    }

    // Get all transactions
    public List<StockTransaction> getAllTransactions() {
        return stockTransactionRepository.findAll();
    }

    // Get transactions by product
    public List<StockTransaction> getTransactionsByProduct(Long productId) {
        return stockTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // Get transactions by type
    public List<StockTransaction> getTransactionsByType(String type) {
        return stockTransactionRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    // Get transaction by ID
    public StockTransaction getTransactionById(Long id) {
        return stockTransactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    // Delete transaction (admin only)
    @Transactional
    public void deleteTransaction(Long id) {
        stockTransactionRepository.deleteById(id);
    }
}
