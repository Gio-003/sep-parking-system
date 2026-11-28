package com.example.psp_backend.dto;

import java.time.LocalDateTime;

public record ClientSummary(
        Long id,
        String name,
        String merchantId,
        String contactEmail,
        String successUrl,
        String failedUrl,
        String errorUrl,
        boolean active,
        LocalDateTime createdAt) {}