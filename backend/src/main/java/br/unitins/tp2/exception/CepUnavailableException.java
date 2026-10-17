package br.unitins.tp2.exception;

public class CepUnavailableException extends RuntimeException {

    public CepUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
