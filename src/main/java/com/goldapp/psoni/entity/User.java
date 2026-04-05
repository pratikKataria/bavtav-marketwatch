package com.goldapp.psoni.entity;

import com.goldapp.psoni.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 100)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @Column(length = 10)
    private String gender;

    @Column
    private LocalDate dateOfBirth;

    @Column(length = 150)
    private String addressLine1;

    @Column(length = 150)
    private String addressLine2;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 100)
    private String country;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.active == null)
            this.active = true;

        if (this.emailVerified == null)
            this.emailVerified = false;

        if (this.authProvider == null)
            this.authProvider = AuthProvider.EMAIL_OTP;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}