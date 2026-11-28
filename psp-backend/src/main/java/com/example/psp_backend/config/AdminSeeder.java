package com.example.psp_backend.config;

import com.example.psp_backend.model.Role;
import com.example.psp_backend.model.UserAccount;
import com.example.psp_backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.password}")
    private String password;

    @Override
    public void run(String... arg){
        if(!users.existsByUsername(username)) {
            users.save(UserAccount.builder()
                    .username(username)
                    .passwordHash(encoder.encode(password))
                    .role(Role.SUPER_ADMIN)
                    .mustChangePassword(true)
                    .build());
        }
    }
}
