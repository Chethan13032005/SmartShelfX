package com.infosys.smartshelfx_backend.repository;

import com.infosys.smartshelfx_backend.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByVendorIdOrderByCreatedAtDesc(Long vendorId);
    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(String status);
    List<PurchaseOrder> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    List<PurchaseOrder> findByVendorEmailOrderByCreatedAtDesc(String vendorEmail);
    List<PurchaseOrder> findTop10ByOrderByCreatedAtDesc();
}
