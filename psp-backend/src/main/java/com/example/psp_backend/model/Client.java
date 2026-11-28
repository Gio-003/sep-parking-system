package com.example.psp_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "merchant_id", nullable = false, unique = true, length = 64)
    private String merchantId;

    @Column(name = "api_key_hash", nullable = false, length = 128)
    private String apiKeyHash;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "success_url", length = 500)
    private String successUrl;

    @Column(name = "failed_url", length = 500)
    private String failedUrl;

    @Column(name = "error_url", length = 500)
    private String errorUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}
