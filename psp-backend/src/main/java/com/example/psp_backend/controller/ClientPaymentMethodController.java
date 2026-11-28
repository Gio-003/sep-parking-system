package com.example.psp_backend.controller;

import com.example.psp_backend.dto.PaymentMethodView;
import com.example.psp_backend.dto.UpdatePaymentMethodStatusRequest;
import com.example.psp_backend.service.ClientPaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/client/payment-methods")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientPaymentMethodController {

    private final ClientPaymentMethodService service;

    @GetMapping
    public List<PaymentMethodView> list(Principal principal) {
        return service.list(principal.getName());
    }

    @PostMapping("/{code}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodView subscribe(@PathVariable String code, Principal principal) {
        return service.subscribe(principal.getName(), code.toUpperCase());
    }

    @PatchMapping("/{code}/active")
    public PaymentMethodView setActive(@PathVariable String code,
                                       @Valid @RequestBody UpdatePaymentMethodStatusRequest request,
                                       Principal principal) {
        return service.setActive(principal.getName(), code.toUpperCase(), request.active());
    }
}