package com.wolfsport.wolfsport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.config.SessionUser;
import com.wolfsport.wolfsport.service.CartService;

import jakarta.servlet.http.HttpSession;

/** Toàn bộ /cart/** đã được AuthInterceptor yêu cầu đăng nhập. */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String view(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.getCart(userId(session)));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
            HttpSession session, RedirectAttributes ra) {
        try {
            cartService.addToCart(userId(session), productId, quantity);
            ra.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String update(@RequestParam Long cartItemId, @RequestParam int quantity,
            HttpSession session, RedirectAttributes ra) {
        try {
            cartService.updateQuantity(userId(session), cartItemId, quantity);
            ra.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long cartItemId, HttpSession session, RedirectAttributes ra) {
        try {
            cartService.removeItem(userId(session), cartItemId);
            ra.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    private Long userId(HttpSession session) {
        return ((SessionUser) session.getAttribute(SessionUser.KEY)).id();
    }
}
