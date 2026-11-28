package com.example.psp_backend.controller;

import com.example.psp_backend.common.security.LoginRateLimiter;
import com.example.psp_backend.common.util.TokenUtils;
import com.example.psp_backend.dto.JwtAuthenticationRequest;
import com.example.psp_backend.dto.UserTokenState;
import com.example.psp_backend.model.UserAccount;
import com.example.psp_backend.service.CustomUserDetailsService;
import com.example.psp_backend.service.UserService;
import com.example.psp_backend.service.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.psp_backend.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService tokenService;

    @Autowired
    private LoginRateLimiter rateLimiter;

    // Prvi endpoint koji pogadja korisnik kada se loguje.
    // Tada zna samo svoje korisnicko ime i lozinku i to prosledjuje na backend.
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            HttpServletRequest request) {

        String ipAddress = request.getRemoteAddr();

        // 1. Provera da li je IP blokiran
        if (rateLimiter.isBlocked(ipAddress)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Previše pokušaja prijave. Pokušajte kasnije.");
        }

        UserAccount user;
        try {
            user = userService.findByUsername(authenticationRequest.getUsername());
        } catch (UsernameNotFoundException ex) {
            rateLimiter.loginFailed(ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Pogrešan email ili lozinka");
        }

        if (!userService.checkPassword(user, authenticationRequest.getPassword())) {
            rateLimiter.loginFailed(ipAddress);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Pogrešan email ili lozinka");
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Potrebno je aktivirati nalog");
        }

        // Ako je login uspešan – reset pokušaja
        rateLimiter.loginSucceeded(ipAddress);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenUtils.generateToken(user.getUsername(), user.getRole().name());
        int expiresIn = tokenUtils.getExpiredIn();

        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Principal principal) {
        userService.changePassword(principal.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok("Lozinka je uspešno promenjena");
    }
}
