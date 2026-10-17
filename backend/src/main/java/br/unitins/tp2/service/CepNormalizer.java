package br.unitins.tp2.service;

public final class CepNormalizer {

    private CepNormalizer() {
    }

    public static String normalize(String cep) {
        if (cep == null) {
            throw new IllegalArgumentException("O CEP deve ser informado.");
        }

        if (!cep.matches("\\d{5}-?\\d{3}")) {
            throw new IllegalArgumentException("O CEP deve conter 8 dígitos.");
        }

        return cep.replace("-", "");
    }
}
