package com.wolfsport.wolfsport.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wolfsport.wolfsport.config.AuthInterceptor;
import com.wolfsport.wolfsport.config.SessionUser;
import com.wolfsport.wolfsport.dto.RegisterForm;
import com.wolfsport.wolfsport.entity.User;
import com.wolfsport.wolfsport.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute(SessionUser.KEY) != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
            HttpServletRequest request, Model model) {
        Optional<User> found = authService.login(email, password);
        if (found.isEmpty()) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng, hoặc tài khoản không được phép đăng nhập");
            model.addAttribute("email", email);
            return "login";
        }

        User user = found.get();
        HttpSession oldSession = request.getSession(true);
        String target = (String) oldSession.getAttribute(AuthInterceptor.REDIRECT_KEY);
        oldSession.removeAttribute(AuthInterceptor.REDIRECT_KEY);

        request.changeSessionId(); // chống session fixation
        request.getSession().setAttribute(SessionUser.KEY,
                new SessionUser(user.getUserId(), user.getFullName(),
                        user.getRole() == null ? "" : user.getRole().trim().toUpperCase()));

        boolean admin = User.ROLE_ADMIN.equalsIgnoreCase(user.getRole() == null ? "" : user.getRole().trim());
        if (isSafeTarget(target) && (admin || !target.startsWith("/admin"))) {
            return "redirect:" + target;
        }
        return admin ? "redirect:/admin/products" : "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model) {
        if (session.getAttribute(SessionUser.KEY) != null) {
            return "redirect:/";
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult result, RedirectAttributes ra) {

        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!result.hasFieldErrors("email") && authService.emailExists(form.getEmail())) {
            result.rejectValue("email", "duplicate", "Email này đã được đăng ký");
        }
        if (result.hasErrors()) {
            return "register";
        }

        authService.register(form);
        ra.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    /** Chỉ chấp nhận đường dẫn nội bộ để tránh open redirect. */
    private boolean isSafeTarget(String target) {
        return target != null && target.startsWith("/") && !target.startsWith("//");
    }
}
