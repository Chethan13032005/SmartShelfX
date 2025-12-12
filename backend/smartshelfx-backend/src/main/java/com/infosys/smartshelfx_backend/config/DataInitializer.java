package com.infosys.smartshelfx_backend.config;

import com.infosys.smartshelfx_backend.model.User;
import com.infosys.smartshelfx_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

/**
 * Initializes default users for testing if they don't exist
 */
@Component
@Order(2) // Run after TriggerRemovalComponent
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Create Admin user if not exists
        if (!userRepository.findByEmail("admin@smartshelfx.com").isPresent()) {
            User admin = new User();
            admin.setEmail("admin@smartshelfx.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setFullName("Admin User");
            admin.setName("Admin User");
            admin.setRole("Admin");
            admin.setCompany("SmartShelfX HQ");
            admin.setPhoneNumber("+1-555-0100");
            admin.setWarehouseLocation("Main Warehouse");
            userRepository.save(admin);
            System.out.println("✅ Created default Admin user: admin@smartshelfx.com");
        }

        // Create Manager user if not exists
        if (!userRepository.findByEmail("manager@smartshelfx.com").isPresent()) {
            User manager = new User();
            manager.setEmail("manager@smartshelfx.com");
            manager.setPassword(passwordEncoder.encode("password123"));
            manager.setFirstName("Manager");
            manager.setLastName("Smith");
            manager.setFullName("Manager Smith");
            manager.setName("Manager Smith");
            manager.setRole("Manager");
            manager.setCompany("SmartShelfX Store");
            manager.setPhoneNumber("+1-555-0101");
            manager.setWarehouseLocation("Store Warehouse");
            userRepository.save(manager);
            System.out.println("✅ Created default Manager user: manager@smartshelfx.com");
        }

        // Create Vendor user if not exists
        if (!userRepository.findByEmail("vendor@smartshelfx.com").isPresent()) {
            User vendor = new User();
            vendor.setEmail("vendor@smartshelfx.com");
            vendor.setPassword(passwordEncoder.encode("password123"));
            vendor.setFirstName("Vendor");
            vendor.setLastName("Johnson");
            vendor.setFullName("Vendor Johnson");
            vendor.setName("Vendor Johnson");
            vendor.setRole("Vendor");
            vendor.setCompany("Supply Co.");
            vendor.setPhoneNumber("+1-555-0102");
            vendor.setWarehouseLocation("Vendor Warehouse");
            userRepository.save(vendor);
            System.out.println("✅ Created default Vendor user: vendor@smartshelfx.com");
        }

        System.out.println("🎉 Data initialization complete! All default users are ready.");
    }
}
