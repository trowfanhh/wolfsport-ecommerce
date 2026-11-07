package com.wolfsport.wolfsport.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.entity.Order;
import com.wolfsport.wolfsport.service.OrderService;

/** Quản lý đơn hàng (/admin/** đã được AuthInterceptor giới hạn cho ADMIN). */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Order> result = orderService.searchAll(status, page);
        model.addAttribute("page", result);
        model.addAttribute("orders", result.getContent());
        model.addAttribute("status", status);
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("order", orderService.findDetail(id));
            return "admin/orders/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, status);
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng #" + id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}