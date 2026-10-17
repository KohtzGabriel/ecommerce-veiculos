package br.unitins.tp2.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, Set<String> roles) {}
