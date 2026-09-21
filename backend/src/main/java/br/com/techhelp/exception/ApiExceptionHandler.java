package br.com.techhelp.exception;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> statusExplicito(org.springframework.web.server.ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode())
                .body(Map.of("erro", erro.getReason() == null ? "Operação não permitida ou registro indisponível" : erro.getReason()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> camposInvalidos(org.springframework.web.bind.MethodArgumentNotValidException erro) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Confira os campos informados e seus limites."));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> erroRegra(
            IllegalArgumentException erro) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(
            NoSuchElementException erro) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", erro.getMessage()));
    }
}