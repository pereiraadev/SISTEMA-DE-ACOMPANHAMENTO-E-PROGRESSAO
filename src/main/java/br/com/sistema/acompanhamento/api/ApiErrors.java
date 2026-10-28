package br.com.sistema.acompanhamento.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiDtos.Erro> negocio(ApiException erro) {
        return ResponseEntity.status(erro.getStatus()).body(new ApiDtos.Erro(erro.getCodigo(), erro.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiDtos.Erro> validacao(MethodArgumentNotValidException erro) {
        return ResponseEntity.badRequest().body(new ApiDtos.Erro("DADOS_INVALIDOS", "Verifique os campos enviados"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiDtos.Erro> conflito(DataIntegrityViolationException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiDtos.Erro("CONFLITO", "Os dados conflitam com um registro existente"));
    }
    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ApiDtos.Erro> entradaInvalida(Exception erro) {
        return ResponseEntity.badRequest().body(new ApiDtos.Erro("DADOS_INVALIDOS", "Verifique os campos enviados"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiDtos.Erro> falhaInesperada(Exception erro) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiDtos.Erro("ERRO_INTERNO", "Não foi possível concluir a operação"));
    }
}
