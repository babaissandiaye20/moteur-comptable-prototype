package com.dmplus.moteurcomptable.shared.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Transforme les erreurs en réponses JSON { code, message } que le front peut afficher. */
@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(MoteurException.class)
    public ResponseEntity<ErreurDto> metier(MoteurException e) {
        return ResponseEntity.status(statutPour(e.code())).body(new ErreurDto(e.code(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurDto> contrainteBase(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErreurDto("CONTRAINTE_BASE", "La base a refusé l'enregistrement : donnée en double ou incohérente"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurDto> jsonInvalide(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ErreurDto("JSON_INVALIDE", "Le corps de la requête ne se lit pas : vérifiez le format (dates AAAA-MM-JJ, montants en nombres)"));
    }

    static HttpStatus statutPour(String code) {
        if (code.equals("COLLISION_IDENTIFIANT") || code.equals("DEJA_EXISTANT") || code.equals("ELEMENT_UTILISE")) {
            return HttpStatus.CONFLICT;
        }
        if (code.endsWith("_INCONNU") || code.endsWith("_INCONNUE")) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}
