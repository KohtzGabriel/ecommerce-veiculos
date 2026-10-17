package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CepNormalizerTest {

    @Test
    void deveNormalizarCepComMascara() {
        assertEquals("77001000", CepNormalizer.normalize("77001-000"));
    }

    @Test
    void deveRejeitarCepComQuantidadeDeDigitosInvalida() {
        assertThrows(IllegalArgumentException.class, () -> CepNormalizer.normalize("7700100"));
    }

    @Test
    void deveRejeitarCepComCaracteresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> CepNormalizer.normalize("77A01-000"));
    }
}
