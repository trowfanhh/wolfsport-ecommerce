package com.wolfsport.wolfsport.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.repository.CartItemRepository;
import com.wolfsport.wolfsport.repository.ProductRepository;

@Service
public class ProductService {

    public static final int CUSTOMER_PAGE_SIZE = 8;
    public static final int ADMIN_PAGE_SIZE = 10;

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CartItemRepository cartItemRepository,
            CategoryService categoryService) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.categoryService = categoryService;
    }

    // ================= Khách hàng =================

    public List<Product> findLatestActive() {
        return productRepository.findTop8ByStatusOrderByProductIdDesc(Product.STATUS_ACTIVE);
    }

    /** Tìm kiếm + lọc theo danh mục + phân trang (chỉ sản phẩm đang bán). */
    public Page<Product> searchActive(String keyword, Long categoryId, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), CUSTOMER_PAGE_SIZE, Sort.by("productId").descending());
        String kw = keyword == null ? "" : keyword.trim();
        if (categoryId == null) {
            return productRepository.searchActive(kw, pageable);
        }
        return productRepository.searchActiveByCategory(kw, categoryId, pageable);
    }

    public Product findActiveById(Long id) {
        return productRepository.findById(id)
                .filter(p -> Product.STATUS_ACTIVE.equals(p.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    public List<Product> findRelated(Product product) {
        return productRepository.findTop4ByCategory_CategoryIdAndStatusAndProductIdNotOrderByProductIdDesc(
                product.getCategory().getCategoryId(), Product.STATUS_ACTIVE, product.getProductId());
    }

    // ================= Admin =================

    public Page<Product> searchAll(String keyword, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), ADMIN_PAGE_SIZE, Sort.by("productId").descending());
        String kw = keyword == null ? "" : keyword.trim();
        return productRepository.findByProductNameContainingIgnoreCase(kw, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    public Product create(Product form, Long categoryId) {
        Product product = new Product();
        copyFields(form, product, categoryId);
        return productRepository.save(product);
    }

    public Product update(Long id, Product form, Long categoryId) {
        Product product = findById(id);
        copyFields(form, product, categoryId);
        return productRepository.save(product);
    }

    /**
     * Xóa sản phẩm. Các dòng giỏ hàng chứa sản phẩm này sẽ bị xóa theo.
     * Nếu sản phẩm đã nằm trong đơn hàng cũ, MySQL sẽ từ chối (khóa ngoại)
     * và ném DataIntegrityViolationException để controller báo lỗi.
     */
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        cartItemRepository.deleteByProduct_ProductId(id);
        productRepository.delete(product);
        productRepository.flush();
    }

    private void copyFields(Product from, Product to, Long categoryId) {
        to.setCategory(categoryService.findById(categoryId));
        to.setProductName(from.getProductName().trim());
        to.setDescription(from.getDescription());
        to.setPrice(from.getPrice());
        to.setStockQuantity(from.getStockQuantity());
        String img = from.getImageUrl();
        to.setImageUrl(img == null || img.isBlank() ? null : img.trim());
        to.setStatus(Product.STATUS_INACTIVE.equals(from.getStatus()) ? Product.STATUS_INACTIVE : Product.STATUS_ACTIVE);
    }
}
