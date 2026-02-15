package com.quant.backend.auth;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonErrorWriter {
    private JsonErrorWriter() {}

    public static void write(HttpServletResponse res, int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType("application/json");

        // Minimal, stabil shape (enkel å parse i Flutter)
        String json = """
                {"error":"%s","message":"%s"}
                """.formatted(escape(error), escape(message));

        res.getWriter().write(json);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
