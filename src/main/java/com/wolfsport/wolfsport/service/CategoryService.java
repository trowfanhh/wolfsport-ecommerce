package com.wolfsport.wolfsport.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.wolfsport.wolfsport.entity.Category;
import com.wolfsport.wolfsport.repository.CategoryRepository;
import com.wolfsport.wolfsport.repository.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by("categoryName"));
    }

    public List<Category> findActive() {
        return categoryRepository.findByStatusOrderByCategoryNameAsc("ACTIVE");
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    /** Kiểm tra trùng tên (bỏ qua chính danh mục đang sửa nếu có id). */
    public boolean isNameTaken(String name, Long excludeId) {
        String trimmed = name.trim();
        if (excludeId == null) {
            return categoryRepository.existsByCategoryNameIgnoreCase(trimmed);
        }
        return categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(trimmed, excludeId);
    }

    public Category create(Category form) {
        Category category = new Category();
        copyFields(form, category);
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category form) {
        Category category = findById(id);
        copyFields(form, category);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = findById(id);
        long count = productRepository.countByCategory_CategoryId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa danh mục \"" + category.getCategoryName()
                    + "\" vì đang có " + count + " sản phẩm. Hãy chuyển hoặc xóa các sản phẩm đó trước, "
                    + "hoặc đổi trạng thái danh mục sang INACTIVE.");
        }
        categoryRepository.delete(category);
    }

    private void copyFields(Category from, Category to) {
        to.setCategoryName(from.getCategoryName().trim());
        to.setDescription(from.getDescription());
        to.setStatus("INACTIVE".equals(from.getStatus()) ? "INACTIVE" : "ACTIVE");
    }
}
