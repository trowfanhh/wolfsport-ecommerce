package com.wolfsport.wolfsport.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.service.CategoryService;
import com.wolfsport.wolfsport.service.ProductService;

import jakarta.validation.Valid;

/** Quản lý sản phẩm (/admin/** đã được AuthInterceptor giới hạn cho ADMIN). */
@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> result = productService.searchAll(keyword, page);
        model.addAttribute("page", result);
        model.addAttribute("products", result.getContent());
        model.addAttribute("keyword", keyword);
        return "admin/products/list";
    }

    @GetMapping("/products/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return prepareForm(model, null, null);
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute("product") Product product, BindingResult result,
            @RequestParam(required = false) Long categoryId, Model model, RedirectAttributes ra) {
        if (categoryId == null) {
            result.reject("category", "Vui lòng chọn danh mục");
        }
        if (result.hasErrors()) {
            return prepareForm(model, null, categoryId);
        }
        productService.create(product, categoryId);
        ra.addFlashAttribute("success", "Đã thêm sản phẩm mới");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return prepareForm(model, id, product.getCategory().getCategoryId());
    }

    @PostMapping("/products/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("product") Product product,
            BindingResult result, @RequestParam(required = false) Long categoryId,
            Model model, RedirectAttributes ra) {
        if (categoryId == null) {
            result.reject("category", "Vui lòng chọn danh mục");
        }
        if (result.hasErrors()) {
            return prepareForm(model, id, categoryId);
        }
        productService.update(id, product, categoryId);
        ra.addFlashAttribute("success", "Đã cập nhật sản phẩm");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa sản phẩm");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Không thể xóa vì sản phẩm đã có trong đơn hàng. "
                    + "Hãy đổi trạng thái sang INACTIVE để ngừng bán.");
        }
        return "redirect:/admin/products";
    }

    private String prepareForm(Model model, Long productId, Long selectedCategoryId) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("productId", productId);
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        return "admin/products/form";
    }
}
