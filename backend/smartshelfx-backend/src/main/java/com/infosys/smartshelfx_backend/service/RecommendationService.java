package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getRestockSuggestions() {
        return inventoryRepository.findLowStockProducts();
    }
}
