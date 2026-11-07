package com.wolfsport.wolfsport.config;

import java.io.Serializable;

/** Thông tin tối thiểu của người dùng đang đăng nhập, lưu trong HttpSession. */
public record SessionUser(Long id, String fullName, String role) implements Serializable {

    public static final String KEY = "currentUser";

    // Getter kiểu JavaBean để Thymeleaf đọc được: ${currentUser.fullName}
    public String getFullName() {
        return fullName;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
