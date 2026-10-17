package br.unitins.tp2.mapper;

import java.util.Set;

import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.model.Pessoa;

public final class UsuarioResponseMapper {

    private UsuarioResponseMapper() {
    }

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return toResponse(usuario.getPessoa(), usuario);
    }

    public static UsuarioResponseDTO toResponse(Pessoa pessoa) {
        return toResponse(pessoa, null);
    }

    public static UsuarioResponseDTO toResponse(Pessoa pessoa, Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario == null ? null : usuario.getId(),
                usuario == null ? null : usuario.getLogin(),
                pessoa.getCpf(), pessoa.getNome(), pessoa.getEmail(),
                pessoa.getGenero(),
                usuario == null ? null : usuario.isAtivo(),
                usuario == null ? null : Set.copyOf(usuario.getPerfis()));
    }
}
