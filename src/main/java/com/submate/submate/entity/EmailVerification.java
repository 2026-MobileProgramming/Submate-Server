package com.submate.submate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String purpose;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    protected EmailVerification() {
    }

    public EmailVerification(
            String email,
            String purpose,
            String code,
            LocalDateTime expiresAt
    ) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = "PENDING";
        this.failCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public int getFailCount() {
        return failCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}