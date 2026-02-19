package com.quant.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class DebugPingController {

    @GetMapping("/debug/ping")
    public String ping() {
        return "pong " + Instant.now().toString();
    }
}