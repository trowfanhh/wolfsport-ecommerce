package com.wolfsport.wolfsport.config;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.wolfsport.wolfsport.entity.Category;
import com.wolfsport.wolfsport.service.CartService;
import com.wolfsport.wolfsport.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Đưa thông tin người dùng đăng nhập và số lượng giỏ hàng vào mọi trang để thanh menu dùng. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final CartService cartService;
    private final CategoryService categoryService;

    public GlobalModelAdvice(CartService cartService, CategoryService categoryService) {
        this.cartService = cartService;
        this.categoryService = categoryService;
    }

    /** Danh mục hiển thị trên thanh điều hướng (trang admin không cần nên bỏ qua). */
    @ModelAttribute("navCategories")
    public List<Category> navCategories(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/admin")) {
            return List.of();
        }
        return categoryService.findActive();
    }

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);
    }

    @ModelAttribute("cartCount")
    public long cartCount(HttpServletRequest request) {
        SessionUser user = currentUser(request);
        return user == null ? 0 : cartService.countItems(user.id());
    }
}
