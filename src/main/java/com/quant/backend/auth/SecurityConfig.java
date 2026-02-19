package com.quant.backend.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                    JsonErrorWriter.write(response, 401, "unauthorized", "Missing or invalid authentication");
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/**",
                                "/h2-console/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        new JwtAuthFilter(jwtService),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );

        // H2 console
        http.headers(h -> h.frameOptions(f -> f.disable()));

        return http.build();
    }
}
