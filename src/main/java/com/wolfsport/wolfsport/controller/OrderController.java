package com.wolfsport.wolfsport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.config.SessionUser;
import com.wolfsport.wolfsport.dto.CheckoutForm;
import com.wolfsport.wolfsport.entity.Cart;
import com.wolfsport.wolfsport.entity.Order;
import com.wolfsport.wolfsport.service.CartService;
import com.wolfsport.wolfsport.service.OrderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/** Toàn bộ /orders/** đã được AuthInterceptor yêu cầu đăng nhập. */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    /** Trang điền thông tin giao hàng. */
    @GetMapping("/checkout")
    public String checkoutForm(HttpSession session, Model model, RedirectAttributes ra) {
        Long userId = userId(session);
        Cart cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng đang trống");
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", orderService.buildCheckoutForm(userId));
        return "orders/checkout";
    }

    /** Xác nhận đặt hàng. */
    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("checkoutForm") CheckoutForm form, BindingResult result,
            HttpSession session, Model model, RedirectAttributes ra) {
        Long userId = userId(session);
        if (result.hasErrors()) {
            model.addAttribute("cart", cartService.getCart(userId));
            return "orders/checkout";
        }
        try {
            Order order = orderService.placeOrder(userId, form);
            ra.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn hàng của bạn là #" + order.getOrderId());
            return "redirect:/orders/" + order.getOrderId();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /** Lịch sử đơn hàng. */
    @GetMapping
    public String list(HttpSession session, Model model) {
        model.addAttribute("orders", orderService.findByUser(userId(session)));
        return "orders/list";
    }

    /** Chi tiết một đơn hàng. */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("order", orderService.findOwnedOrder(userId(session), id));
            return "orders/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
    }

    /** Khách tự hủy đơn khi còn chờ xác nhận. */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            orderService.cancelByCustomer(userId(session), id);
            ra.addFlashAttribute("success", "Đã hủy đơn hàng #" + id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    private Long userId(HttpSession session) {
        return ((SessionUser) session.getAttribute(SessionUser.KEY)).id();
    }
}