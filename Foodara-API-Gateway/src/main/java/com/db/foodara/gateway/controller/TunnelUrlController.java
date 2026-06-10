package com.db.foodara.gateway.controller;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class TunnelUrlController {

    private final Environment env;

    public TunnelUrlController(Environment env) {
        this.env = env;
    }

    @GetMapping("/api/public-url")
    public Map<String, String> publicUrl() {
        String url = System.getenv("TUNNEL_URL");
        if (url == null || url.isBlank()) {
            url = System.getProperty("tunnel.url", "http://localhost:8080");
        }
        return Map.of("publicUrl", url);
    }
}
