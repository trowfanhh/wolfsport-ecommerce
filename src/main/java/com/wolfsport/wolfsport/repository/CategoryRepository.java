package com.wolfsport.wolfsport.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wolfsport.wolfsport.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStatusOrderByCategoryNameAsc(String status);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(String categoryName, Long categoryId);
}
