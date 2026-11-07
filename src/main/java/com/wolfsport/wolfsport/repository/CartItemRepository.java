package com.wolfsport.wolfsport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wolfsport.wolfsport.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);

    @Query("select count(ci) from CartItem ci where ci.cart.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    void deleteByProduct_ProductId(Long productId);
}
