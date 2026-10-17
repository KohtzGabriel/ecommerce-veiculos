package br.unitins.tp2.exception;

public class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
        super("Login ou senha inválidos.");
    }
}
