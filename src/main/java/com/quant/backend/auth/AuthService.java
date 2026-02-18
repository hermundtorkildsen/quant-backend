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
        String username = req.getUsername() == null ? "" : req.getUsername().trim().toLowerCase();
        String password = req.getPassword();

        if (email.isBlank() || password == null || password.length() < 10) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (username.isBlank() || username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (!username.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers and underscore");
        }

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        UserEntity user = new UserEntity(
                UUID.randomUUID().toString(),
                username,
                email,
                encoder.encode(password)
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
