package com.quant.backend.controller;

import com.quant.backend.auth.QuantPrincipal;
import com.quant.backend.auth.UserEntity;
import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.dto.UserMeResponse;
import org.springframework.http.HttpStatus;
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

        Object p = authentication.getPrincipal();

        if (!(p instanceof QuantPrincipal qp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(qp.userId())
                .map(u -> ResponseEntity.ok(new UserMeResponse(u.getEmail(), u.getUsername())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
