package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryRepository repo;

    public InventoryController(InventoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/view")
    public List<Inventory> getAll() {
        return repo.findAll();
    }

    @PostMapping("/add")
    public Inventory create(@RequestBody Inventory item) {
        return repo.save(item);
    }

    @PutMapping("/update/{id}")
    public Inventory update(@PathVariable Long id, @RequestBody Inventory item) {
        Inventory existing = repo.findById(id).orElseThrow();
        existing.setProductName(item.getProductName());
        existing.setCategory(item.getCategory());
        existing.setQuantity(item.getQuantity());
        existing.setPrice(item.getPrice());
        existing.setSupplier(item.getSupplier());
        existing.setLocation(item.getLocation());
        return repo.save(existing);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
