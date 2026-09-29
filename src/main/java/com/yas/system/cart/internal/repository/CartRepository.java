package com.yas.system.cart.internal.repository;

import com.yas.system.cart.internal.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByCustomerId(String customerId);

    Optional<Cart> findByCustomerIdAndProductVariantId(String customerId, Long productVariantId);

    Optional<Cart> findByIdAndCustomerId(Long id, String customerId);

    @Modifying
    void deleteByCustomerId(String customerId);

    @Modifying
    void deleteByIdAndCustomerId(Long id, String customerId);
}
