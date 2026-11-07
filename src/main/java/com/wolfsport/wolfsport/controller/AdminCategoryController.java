package com.wolfsport.wolfsport.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.entity.Category;
import com.wolfsport.wolfsport.service.CategoryService;

import jakarta.validation.Valid;

/** Quản lý danh mục (/admin/** đã được AuthInterceptor giới hạn cho ADMIN). */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categoryId", null);
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("category") Category category, BindingResult result,
            Model model, RedirectAttributes ra) {
        checkDuplicate(category, null, result);
        if (result.hasErrors()) {
            model.addAttribute("categoryId", null);
            return "admin/categories/form";
        }
        categoryService.create(category);
        ra.addFlashAttribute("success", "Đã thêm danh mục mới");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("categoryId", id);
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("category") Category category,
            BindingResult result, Model model, RedirectAttributes ra) {
        checkDuplicate(category, id, result);
        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/categories/form";
        }
        categoryService.update(id, category);
        ra.addFlashAttribute("success", "Đã cập nhật danh mục");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa danh mục");
        } catch (IllegalStateException | DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", e instanceof IllegalStateException
                    ? e.getMessage() : "Không thể xóa danh mục này vì còn dữ liệu liên quan");
        }
        return "redirect:/admin/categories";
    }

    private void checkDuplicate(Category category, Long excludeId, BindingResult result) {
        if (!result.hasFieldErrors("categoryName") && categoryService.isNameTaken(category.getCategoryName(), excludeId)) {
            result.rejectValue("categoryName", "duplicate", "Tên danh mục đã tồn tại");
        }
    }
}
