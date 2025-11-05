package com.infosys.smartshelfx_backend.repository;

import com.infosys.smartshelfx_backend.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<StockTransaction> findTop10ByOrderByCreatedAtDesc();
}
