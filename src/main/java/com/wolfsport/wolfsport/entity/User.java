package com.wolfsport.wolfsport.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String STATUS_ACTIVE = "ACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String address;

    @Column(nullable = false, length = 20)
    private String role = ROLE_CUSTOMER;

    @Column(nullable = false, length = 20)
    private String status = STATUS_ACTIVE;

    // Hai cột này do MySQL tự điền (DEFAULT CURRENT_TIMESTAMP) nên chỉ đọc
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
