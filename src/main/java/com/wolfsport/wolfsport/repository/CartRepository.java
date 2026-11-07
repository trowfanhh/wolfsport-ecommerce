package com.wolfsport.wolfsport.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wolfsport.wolfsport.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_UserId(Long userId);

    /** Lấy giỏ hàng kèm luôn các item và sản phẩm để hiển thị. */
    @Query("select c from Cart c "
            + "left join fetch c.items i "
            + "left join fetch i.product "
            + "where c.user.userId = :userId "
            + "order by i.cartItemId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") Long userId);
}
