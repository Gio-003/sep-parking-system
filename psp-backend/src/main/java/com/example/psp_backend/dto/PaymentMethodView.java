package com.example.psp_backend.dto;

public record PaymentMethodView(
        String code,
        String name,
        boolean available,
        boolean subscribed,
        boolean active) {}
