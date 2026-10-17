package br.unitins.tp2.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.unitins.tp2.dto.AuthRequest;
import br.unitins.tp2.dto.AuthResponse;
import br.unitins.tp2.exception.AuthenticationException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {
    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    @Inject UsuarioRepository usuarios;
    @Inject SenhaService senhas;
    @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer;

    @Override
    public AuthResponse login(AuthRequest request) {
        Usuario usuario = usuarios.findByLogin(request.login().strip());
        if (usuario == null || !usuario.isAtivo()
                || !senhas.verificar(usuario.getLogin(), request.senha(), usuario.getSenha())) {
            throw new AuthenticationException();
        }

        Set<String> roles = usuario.getPerfis().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (roles.isEmpty()) throw new AuthenticationException();

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        String token = Jwt.issuer(issuer)
                .upn(usuario.getLogin())
                .subject(usuario.getId().toString())
                .groups(roles)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .sign();
        return new AuthResponse(token, "Bearer", expiresAt, roles);
    }
}
