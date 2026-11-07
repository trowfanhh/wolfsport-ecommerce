package com.wolfsport.wolfsport.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import com.wolfsport.wolfsport.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ----- Phía khách hàng: chỉ lấy sản phẩm đang bán (ACTIVE) -----

    @Query("select p from Product p where p.status = 'ACTIVE' "
            + "and lower(p.productName) like lower(concat('%', :keyword, '%'))")
    Page<Product> searchActive(@Param("keyword") String keyword, Pageable pageable);

    @Query("select p from Product p where p.status = 'ACTIVE' "
            + "and p.category.categoryId = :categoryId "
            + "and lower(p.productName) like lower(concat('%', :keyword, '%'))")
    Page<Product> searchActiveByCategory(@Param("keyword") String keyword,
            @Param("categoryId") Long categoryId, Pageable pageable);

    List<Product> findTop8ByStatusOrderByProductIdDesc(String status);

    List<Product> findTop4ByCategory_CategoryIdAndStatusAndProductIdNotOrderByProductIdDesc(
            Long categoryId, String status, Long productId);

    // ----- Phía admin: lấy tất cả trạng thái -----

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    long countByCategory_CategoryId(Long categoryId);

     /** Trừ kho; trả về 0 nếu không đủ hàng. Câu lệnh nguyên tử nên an toàn khi nhiều người mua cùng lúc. */
    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity - :qty "
            + "where p.productId = :id and p.stockQuantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    /** Hoàn kho khi hủy đơn. */
    @Modifying
    @Query("update Product p set p.stockQuantity = p.stockQuantity + :qty where p.productId = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
