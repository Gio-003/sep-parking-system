package com.example.psp_backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePaymentMethodStatusRequest(@NotNull Boolean active) {}