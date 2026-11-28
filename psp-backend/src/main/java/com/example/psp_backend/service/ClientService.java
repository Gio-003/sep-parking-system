package com.example.psp_backend.service;

import com.example.psp_backend.dto.ClientSummary;
import com.example.psp_backend.dto.RegisterClientRequest;
import com.example.psp_backend.dto.RegisterClientResponse;
import com.example.psp_backend.common.exception.BusinessException;
import com.example.psp_backend.model.Client;
import com.example.psp_backend.model.ClientPaymentMethod;
import com.example.psp_backend.model.PaymentMethod;
import com.example.psp_backend.model.Role;
import com.example.psp_backend.model.UserAccount;
import com.example.psp_backend.repository.ClientPaymentMethodRepository;
import com.example.psp_backend.repository.ClientRepository;
import com.example.psp_backend.repository.PaymentMethodRepository;
import com.example.psp_backend.repository.UserAccountRepository;
import com.example.psp_backend.common.util.ApiKeyGenerator;
import com.example.psp_backend.common.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserAccountRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ClientPaymentMethodRepository clientPaymentMethodRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Transactional
    public RegisterClientResponse register(RegisterClientRequest req) {

        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Korisničko ime već postoji");
        }

        // 1. generisanje kredencijala
        String merchantId = generateUniqueMerchantId();
        String apiKey = ApiKeyGenerator.generateApiKey();
        String temporaryPassword = PasswordGenerator.generateTemporaryPassword();

        // 2. Client (čuva se samo hash API ključa)
        Client client = clientRepository.save(Client.builder()
                .name(req.name())
                .merchantId(merchantId)
                .apiKeyHash(ApiKeyGenerator.sha256(apiKey))
                .email(req.contactEmail())
                .successUrl(null)
                .failedUrl(null)
                .errorUrl(null)
                .build());

        // 3. UserAccount za predstavnika klijenta (čuva se BCrypt hash lozinke)
        UserAccount user = userRepository.save(UserAccount.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .role(Role.CLIENT)
                .client(client)
                .mustChangePassword(true)
                .build());

        sendEmail(client, user.getUsername(), temporaryPassword, apiKey);

        // 4. podrazumevana metoda: CARD, da klijent nikad nema 0 aktivnih metoda
        PaymentMethod card = paymentMethodRepository.findByCode("CARD")
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Metoda CARD nije podešena"));
        clientPaymentMethodRepository.save(ClientPaymentMethod.builder()
                .client(client)
                .paymentMethod(card)
                .active(true)
                .build());

        // 5. plain vrednosti se vraćaju samo ovde, jednom
        return new RegisterClientResponse(
                client.getId(), merchantId, apiKey, req.username(), temporaryPassword);
    }

    @Transactional(readOnly = true)
    public List<ClientSummary> findAll() {
        return clientRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional
    public ClientSummary setActive(Long clientId, boolean active) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Klijent ne postoji"));
        client.setActive(active);
        return toSummary(client);
    }

    private String generateUniqueMerchantId() {
        String id;
        do {
            id = ApiKeyGenerator.generateMerchantId();
        } while (clientRepository.existsByMerchantId(id));
        return id;
    }

    private ClientSummary toSummary(Client c) {
        return new ClientSummary(c.getId(), c.getName(), c.getMerchantId(), c.getEmail(),
                c.getSuccessUrl(), c.getFailedUrl(), c.getErrorUrl(), c.isActive(), c.getCreatedAt());
    }

    public void sendEmail(Client client, String username, String temporaryPassword, String apiKey) {

        String recipientAddress = client.getEmail();
        String subject = "Registration on parking service website";
        String loginUrl = "http://localhost:4200/login";
        String message = "You have successfully registered on the Parking Service website.\n\n" +
                "We have sent you the credentials and information required for login and future transactions. Please save them in a secure place.\n\n" +
                "Username: " + username + "\n" +
                "Temporary Password: " + temporaryPassword + "\n" +
                "Merchant ID: " + client.getMerchantId() + "\n" +
                "API Key: " + apiKey + "\n";


        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}