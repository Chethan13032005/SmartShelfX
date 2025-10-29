package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryRepository repo;

    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view")
    public List<Inventory> getAll() {
        return repo.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public Inventory create(@RequestBody Inventory item) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";
        item.setCreatedBy(username);
        item.setUpdatedBy(username);
        item.setUpdatedAt(LocalDateTime.now());
        return repo.save(item);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update/{id}")
    public Inventory update(@PathVariable Long id, @RequestBody Inventory item) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";
        
        Inventory existing = repo.findById(id).orElseThrow();
        existing.setProductName(item.getProductName());
        existing.setCategory(item.getCategory());
        existing.setQuantity(item.getQuantity());
        existing.setPrice(item.getPrice());
        existing.setSupplier(item.getSupplier());
        existing.setLocation(item.getLocation());
        existing.setImageUrl(item.getImageUrl());
        existing.setUpdatedBy(username);
        existing.setUpdatedAt(LocalDateTime.now());
        return repo.save(existing);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
