package com.infosys.smartshelfx_backend.repository;

import com.infosys.smartshelfx_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {}
