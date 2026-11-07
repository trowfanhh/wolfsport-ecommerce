package com.wolfsport.wolfsport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.wolfsport.wolfsport.service.CategoryService;
import com.wolfsport.wolfsport.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findActive());
        model.addAttribute("products", productService.findLatestActive());
        return "index";
    }
}
