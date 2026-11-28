package com.example.psp_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,

        @NotBlank
        @Size(min = 12, max = 100, message = "Lozinka mora imati najmanje 12 karaktera")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Lozinka mora sadržati malo slovo, veliko slovo i cifru")
        String newPassword) {}
