package br.unitins.tp2.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SenhaService {

    private static final String FORMATO = "pbkdf2-sha256-email-v1";
    private static final int ITERACOES = 600_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarHash(String email, String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = calcular(email, senha, salt);
        return FORMATO + "$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String email, String senha, String hashArmazenado) {
        if (email == null || senha == null || hashArmazenado == null) {
            return false;
        }
        String[] partes = hashArmazenado.split("\\$", -1);
        if (partes.length != 4 || !FORMATO.equals(partes[0])
                || !Integer.toString(ITERACOES).equals(partes[1])) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (salt.length != TAMANHO_SALT || esperado.length != TAMANHO_HASH_BITS / 8) {
                return false;
            }
            return MessageDigest.isEqual(esperado, calcular(email, senha, salt));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] calcular(String email, String senha, byte[] salt) {
        // Salt aleatório com tamanho fixo, seguido do e-mail canônico como contexto.
        byte[] contexto = email.strip().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        byte[] saltComEmail = Arrays.copyOf(salt, salt.length + contexto.length);
        System.arraycopy(contexto, 0, saltComEmail, salt.length, contexto.length);
        char[] caracteres = senha.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(caracteres, saltComEmail, ITERACOES, TAMANHO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível calcular o hash da senha.", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
