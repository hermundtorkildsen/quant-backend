package com.quant.backend.dto;

import java.time.Instant;

public record UserMeResponse(
        String email,
        String username,
        Instant createdAt,
        Instant lastLoginAt
) {}
