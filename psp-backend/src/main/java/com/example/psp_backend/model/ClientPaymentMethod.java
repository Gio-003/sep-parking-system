package com.example.psp_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_payment_method",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "payment_method_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;
}
