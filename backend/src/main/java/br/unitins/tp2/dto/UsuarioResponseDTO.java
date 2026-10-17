package br.unitins.tp2.dto;

import java.util.Set;

import br.unitins.tp2.model.Perfil;

public record UsuarioResponseDTO(
        Long id,
        String login,
        String cpf,
        String nome,
        String email,
        String genero,
        Boolean ativo,
        Set<Perfil> perfis) {
}
