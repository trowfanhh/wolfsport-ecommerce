package com.wolfsport.wolfsport.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wolfsport.wolfsport.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Lịch sử đơn hàng của một khách, đơn mới nhất lên đầu. */
    List<Order> findByUser_UserIdOrderByOrderIdDesc(Long userId);

    /** Lấy đơn kèm các dòng sản phẩm và người đặt để hiển thị chi tiết. */
    @EntityGraph(attributePaths = { "items", "user" })
    Optional<Order> findDetailByOrderId(Long orderId);

    // ----- Dùng cho admin (Bước 4) -----
    @EntityGraph(attributePaths = "user")
    Page<Order> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Order> findByStatus(String status, Pageable pageable);
}