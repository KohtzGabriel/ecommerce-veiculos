package br.unitins.tp2.dto;

public record CepResponseDTO(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String estado) {
}
