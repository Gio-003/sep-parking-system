package com.example.psp_backend.repository;

import com.example.psp_backend.model.ClientPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientPaymentMethodRepository extends JpaRepository<ClientPaymentMethod, Long> {
    List<ClientPaymentMethod> findByClientId(Long clientId);
    Optional<ClientPaymentMethod> findByClientIdAndPaymentMethodCode(Long clientId, String code);
    long countByClientIdAndActiveTrue(Long clientId);
}