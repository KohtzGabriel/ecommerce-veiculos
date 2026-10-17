package br.unitins.tp2.dto;

public record CepResponseDTO(
        String cep,
        String logradouro,
        String bairro,
        Long municipioId,
        String municipio,
        String estado) {
}
