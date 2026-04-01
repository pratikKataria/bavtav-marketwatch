package com.goldapp.psoni.entity;

import com.goldapp.psoni.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_otp", indexes = {
        @Index(name = "idx_email_otp_email", columnList = "email"),
        @Index(name = "idx_email_otp_expiry", columnList = "expiresAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String referenceId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.used == null) this.used = false;
        if (this.attemptCount == null) this.attemptCount = 0;
    }
}