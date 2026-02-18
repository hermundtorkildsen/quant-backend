package com.quant.backend.controller;

import com.quant.backend.auth.UserEntity;
import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.dto.UserMeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserJpaRepository userRepository;

    public UserController(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {

        // I ditt oppsett er dette vanligvis email fra JWT
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                new UserMeResponse(user.getEmail(), user.getUsername())
        );
    }
}
