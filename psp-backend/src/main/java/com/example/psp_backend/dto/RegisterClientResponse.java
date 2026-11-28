package com.example.psp_backend.dto;

public record RegisterClientResponse(
        Long clientId,
        String merchantId,
        String apiKey,             // plain, prikazuje se samo jednom
        String username,
        String temporaryPassword   // plain, prikazuje se samo jednom
) {}