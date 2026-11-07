package com.wolfsport.wolfsport.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.service.CategoryService;
import com.wolfsport.wolfsport.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    /** Danh sách sản phẩm + tìm kiếm + lọc danh mục + phân trang. */
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<Product> result = productService.searchActive(keyword, categoryId, page);
        model.addAttribute("page", result);
        model.addAttribute("products", result.getContent());
        model.addAttribute("categories", categoryService.findActive());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productService.findActiveById(id);
        model.addAttribute("product", product);
        model.addAttribute("related", productService.findRelated(product));
        model.addAttribute("categoryId", product.getCategory().getCategoryId()); // để menu làm nổi bật danh mục
        return "products/detail";
    }
}
