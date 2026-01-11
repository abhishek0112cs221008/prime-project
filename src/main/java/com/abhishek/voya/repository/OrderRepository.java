package com.abhishek.voya.repository;

import com.abhishek.voya.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserEmail(String userEmail);

    boolean existsByUtr(String utr);
}
