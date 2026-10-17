package br.unitins.tp2.service;

import br.unitins.tp2.dto.AuthRequest;
import br.unitins.tp2.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
}
