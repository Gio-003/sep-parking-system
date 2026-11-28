package com.example.psp_backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateClientStatusRequest(@NotNull Boolean active) {}
