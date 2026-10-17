package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SenhaServiceTest {

    private final SenhaService service = new SenhaService();

    @Test
    void deveVincularHashASenhaEEmailComSaltAleatorio() {
        String senha = "Uma senha longa de teste!";
        String hash = service.gerarHash("ana@example.com", senha);

        assertTrue(hash.length() <= 255);
        assertNotEquals(senha, hash);
        assertTrue(service.verificar("ana@example.com", senha, hash));
        assertTrue(service.verificar(" ANA@example.com ", senha, hash));
        assertFalse(service.verificar("outra@example.com", senha, hash));
        assertFalse(service.verificar("ana@example.com", "outra senha", hash));
        assertNotEquals(hash, service.gerarHash("ana@example.com", senha));
    }

    @Test
    void deveRejeitarHashMalformado() {
        assertFalse(service.verificar("ana@example.com", "senha", null));
        assertFalse(service.verificar("ana@example.com", "senha", "senha-em-texto"));
        assertFalse(service.verificar("ana@example.com", "senha", "pbkdf2-sha256-email-v1$600000$!$!"));
        assertFalse(service.verificar("ana@example.com", "senha", "pbkdf2-sha256-email-v1$999999999$AA==$AA=="));
    }
}
