package com.dmplus.moteurcomptable.shared.exception;

/** Erreur métier du moteur : l'événement ne peut pas devenir une écriture. */
public class MoteurException extends RuntimeException {

    private final String code;

    public MoteurException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
