package com.example.psp_backend.service;

import com.example.psp_backend.model.UserAccount;
import com.example.psp_backend.model.VerificationToken;
import com.example.psp_backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public VerificationToken createToken(UserAccount user, String token) {
        VerificationToken verificationToken = new VerificationToken(user, token, new java.util.Date(System.currentTimeMillis() + 24*60*60*1000)); // 24h
        return tokenRepository.save(verificationToken);
    }

    public VerificationToken findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void deleteToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
}
