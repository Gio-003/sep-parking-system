package com.example.psp_backend.config;

import com.example.psp_backend.model.PaymentMethod;
import com.example.psp_backend.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentMethodSeeder implements CommandLineRunner {

    private final PaymentMethodRepository repository;

    @Override
    public void run(String... args) {
        Map<String, String> methods = Map.of(
                "CARD", "Plaćanje platnom karticom",
                "QR", "Plaćanje QR kodom (IPS)",
                "PAYPAL", "PayPal",
                "CRYPTO", "Kriptovalute"
        );

        methods.forEach((code, name) -> {
            if (repository.findByCode(code).isEmpty()) {
                repository.save(PaymentMethod.builder()
                        .code(code)
                        .name(name)
                        .build());
            }
        });
    }
}