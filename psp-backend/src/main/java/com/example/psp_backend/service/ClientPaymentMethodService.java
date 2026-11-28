package com.example.psp_backend.service;

import com.example.psp_backend.dto.PaymentMethodView;
import com.example.psp_backend.common.exception.BusinessException;
import com.example.psp_backend.model.Client;
import com.example.psp_backend.model.ClientPaymentMethod;
import com.example.psp_backend.model.PaymentMethod;
import com.example.psp_backend.model.UserAccount;
import com.example.psp_backend.repository.ClientPaymentMethodRepository;
import com.example.psp_backend.repository.ClientRepository;
import com.example.psp_backend.repository.PaymentMethodRepository;
import com.example.psp_backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientPaymentMethodService {

    private final ClientRepository clientRepository;
    private final UserAccountRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ClientPaymentMethodRepository clientPaymentMethodRepository;

    @Transactional(readOnly = true)
    public List<PaymentMethodView> list(String username) {
        return buildViews(resolveClient(username).getId());
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodView> listForClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Klijent ne postoji");
        }
        return buildViews(clientId);
    }


    @Transactional
    public PaymentMethodView subscribe(String username, String code) {
        Client client = resolveClient(username);
        PaymentMethod pm = paymentMethodRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Metoda ne postoji"));

        if (!pm.isAvailable()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Metoda trenutno nije dostupna");
        }
        if (clientPaymentMethodRepository
                .findByClientIdAndPaymentMethodCode(client.getId(), code).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Već ste pretplaćeni na ovu metodu");
        }

        clientPaymentMethodRepository.save(ClientPaymentMethod.builder()
                .client(client)
                .paymentMethod(pm)
                .active(true)
                .build());

        return new PaymentMethodView(pm.getCode(), pm.getName(), true, true, true);
    }


    @Transactional
    public PaymentMethodView setActive(String username, String code, boolean active) {
        Client client = resolveClient(username);
        ClientPaymentMethod cpm = clientPaymentMethodRepository
                .findByClientIdAndPaymentMethodCode(client.getId(), code)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "Niste pretplaćeni na ovu metodu"));

        if (cpm.isActive() && !active
                && clientPaymentMethodRepository.countByClientIdAndActiveTrue(client.getId()) <= 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Mora ostati bar jedna aktivna metoda plaćanja");
        }
        if (!cpm.isActive() && active && !cpm.getPaymentMethod().isAvailable()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Metoda trenutno nije dostupna");
        }

        cpm.setActive(active);
        PaymentMethod pm = cpm.getPaymentMethod();
        return new PaymentMethodView(pm.getCode(), pm.getName(), pm.isAvailable(), true, active);
    }


    private List<PaymentMethodView> buildViews(Long clientId) {
        Map<String, ClientPaymentMethod> mine = clientPaymentMethodRepository
                .findByClientId(clientId).stream()
                .collect(Collectors.toMap(c -> c.getPaymentMethod().getCode(), Function.identity()));

        return paymentMethodRepository.findAll().stream()
                .map(pm -> {
                    ClientPaymentMethod cpm = mine.get(pm.getCode());
                    return new PaymentMethodView(pm.getCode(), pm.getName(), pm.isAvailable(),
                            cpm != null, cpm != null && cpm.isActive());
                })
                .toList();
    }

    private Client resolveClient(String username) {
        UserAccount user = userRepository.findByUsername(username);
        if (user == null || user.getClient() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Nalog nije povezan sa klijentom");
        }
        return user.getClient();
    }
}