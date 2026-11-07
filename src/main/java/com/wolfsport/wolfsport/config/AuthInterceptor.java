package com.wolfsport.wolfsport.config;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Chặn truy cập khi chưa đăng nhập (hoặc không đủ quyền ADMIN).
 * Dùng cho /cart/** (cần đăng nhập) và /admin/** (cần quyền ADMIN).
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String REDIRECT_KEY = "redirectAfterLogin";

    private final boolean adminOnly;

    public AuthInterceptor(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SessionUser.KEY);

        if (user == null) {
            // Nhớ trang đang muốn vào (chỉ với GET) để quay lại sau khi đăng nhập
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String target = request.getRequestURI()
                        + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
                request.getSession(true).setAttribute(REDIRECT_KEY, target);
            }
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }

        if (adminOnly && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
