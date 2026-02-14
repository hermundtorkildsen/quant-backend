package com.quant.backend.auth;

import com.quant.backend.auth.dto.AuthResponse;
import com.quant.backend.auth.dto.LoginRequest;
import com.quant.backend.auth.dto.RegisterRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserJpaRepository userRepo;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserJpaRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();

        if (email.isBlank() || req.getPassword() == null || req.getPassword().length() < 10) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserEntity user = new UserEntity(
                UUID.randomUUID().toString(),
                email,
                encoder.encode(req.getPassword())
        );

        userRepo.save(user);

        String token = jwtService.createToken(user.getId(), user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();

        UserEntity user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.createToken(user.getId(), user.getEmail());
        return new AuthResponse(token);
    }
}
