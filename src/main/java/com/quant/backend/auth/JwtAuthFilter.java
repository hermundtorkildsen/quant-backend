package com.quant.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(jwtService.getKey())
                        .requireIssuer(jwtService.getIssuer())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();
                String email = claims.get("email", String.class);

                QuantPrincipal principal = new QuantPrincipal(userId, email);

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Invalid/expired token -> returner 401 (ikke fall gjennom silent)
                SecurityContextHolder.clearContext();
                JsonErrorWriter.write(response, 401, "unauthorized", "Invalid or expired token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // TODO: SHOULD BE REMOVED IN PROD????
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
