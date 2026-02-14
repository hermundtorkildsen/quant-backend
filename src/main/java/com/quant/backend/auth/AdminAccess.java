package com.quant.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminAccess {

    private final Set<String> adminEmails;

    public AdminAccess(@Value("${quant.admin.emails:}") String emailsCsv) {
        this.adminEmails = Arrays.stream(emailsCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    public boolean isAdminEmail(String email) {
        if (email == null) return false;
        return adminEmails.contains(email.trim().toLowerCase());
    }
}
