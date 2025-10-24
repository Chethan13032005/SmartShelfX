package com.infosys.smartshelfx_backend.controller;

import com.infosys.smartshelfx_backend.model.Inventory;
import com.infosys.smartshelfx_backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InventoryWebController {
    
    @Autowired
    private InventoryRepository repository;
    
    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("listProducts", repository.findAll());
        return "index";
    }
    
    @GetMapping("/showNewProductForm")
    public String showNewProductForm(Model model) {
        Inventory product = new Inventory();
        model.addAttribute("product", product);
        return "addProduct";
    }
    
    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute("product") Inventory product) {
        repository.save(product);
        return "redirect:/";
    }
    
    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable(value = "id") Long id, Model model) {
        Inventory product = repository.findById(id).orElseThrow();
        model.addAttribute("product", product);
        return "editProduct";
    }
    
    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable(value = "id") Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }
}
