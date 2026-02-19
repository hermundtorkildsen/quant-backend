package com.quant.backend;

import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.auth.UserEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug")
public class DebugMaintenanceController {

    private final UserJpaRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DebugMaintenanceController(UserJpaRepository userRepo) {
        this.userRepo = userRepo;
    }

    public record ResetPasswordRequest(String email, String newPassword) {}

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest req) {
        String email = req.email() == null ? "" : req.email().trim().toLowerCase();
        String newPassword = req.newPassword();

        if (email.isBlank() || newPassword == null || newPassword.length() < 10) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        UserEntity user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);

        return "OK";
    }
}
