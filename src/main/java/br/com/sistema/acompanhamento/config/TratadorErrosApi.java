package br.com.sistema.acompanhamento.config;

import br.com.sistema.acompanhamento.auth.AuthController;
import br.com.sistema.acompanhamento.auth.CredenciaisInvalidasException;
import br.com.sistema.acompanhamento.auth.EmailJaCadastradoException;
import br.com.sistema.acompanhamento.comparativo.ComparativoController;
import br.com.sistema.acompanhamento.dashboard.DashboardController;
import br.com.sistema.acompanhamento.historico.HistoricoController;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// devolve os erros dos nossos controllers em json {"erro": "..."}
// limitei so pra eles pra nao mudar o comportamento dos outros
@RestControllerAdvice(assignableTypes = {
        AuthController.class, HistoricoController.class, ComparativoController.class, DashboardController.class})
public class TratadorErrosApi {

    private static final Logger log = LoggerFactory.getLogger(TratadorErrosApi.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> dadoInvalido(IllegalArgumentException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // quando o id na url nao e numero
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> parametroFaltando(MissingServletRequestParameterException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + e.getParameterName() + ".");
    }

    // falhou alguma validacao do DTO (@NotBlank, @Email, @Size)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> dtoInvalido(MethodArgumentNotValidException e) {
        FieldError primeiro = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String mensagem = primeiro != null ? primeiro.getDefaultMessage() : "Dados inválidos.";
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    // json quebrado ou corpo vazio
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(NoSuchElementException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResposta> emailJaCadastrado(EmailJaCadastradoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    // dois cadastros iguais ao mesmo tempo: o banco barra pela constraint de email unico
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> conflitoNoBanco(DataIntegrityViolationException e) {
        return resposta(HttpStatus.CONFLICT, "E-mail já cadastrado.");
    }

    // qualquer outro erro: log no servidor e mensagem generica pro cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroInesperado(Exception e) {
        log.error("Erro inesperado na API", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(mensagem));
    }
}
