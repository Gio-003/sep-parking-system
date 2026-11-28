package com.example.psp_backend.controller;

import com.example.psp_backend.dto.ClientSummary;
import com.example.psp_backend.dto.RegisterClientRequest;
import com.example.psp_backend.dto.RegisterClientResponse;
import com.example.psp_backend.dto.UpdateClientStatusRequest;
import com.example.psp_backend.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminClientController {

    private final ClientService clientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterClientResponse register(@Valid @RequestBody RegisterClientRequest request){
        return clientService.register(request);
    }

    @GetMapping
    public List<ClientSummary> findAll(){
        return clientService.findAll();
    }

    @PatchMapping("{id}/status")
    public ClientSummary setStatus(@PathVariable Long id, @Valid @RequestBody UpdateClientStatusRequest request){
        return clientService.setActive(id, request.active());
    }
}
