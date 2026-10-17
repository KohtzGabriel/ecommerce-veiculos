package br.unitins.tp2.dto;

public record TelefoneResponseDTO(
    String codigoArea,
    String numero,
    boolean whatsapp
) {
}