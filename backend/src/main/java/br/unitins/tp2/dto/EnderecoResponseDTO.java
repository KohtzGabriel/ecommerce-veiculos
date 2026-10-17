package br.unitins.tp2.dto;

public record EnderecoResponseDTO(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado) {
}
