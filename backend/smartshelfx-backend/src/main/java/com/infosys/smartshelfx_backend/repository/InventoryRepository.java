package com.infosys.smartshelfx_backend.repository;

import com.infosys.smartshelfx_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	long countByQuantityLessThanEqual(int quantity);

	@Query("SELECT COUNT(DISTINCT i.supplier) FROM Inventory i WHERE i.supplier IS NOT NULL AND i.supplier <> ''")
	long countDistinctSuppliers();

	@Query("SELECT i FROM Inventory i WHERE i.quantity <= COALESCE(i.reorderLevel, 10)")
	List<Inventory> findLowStockProducts();
}
