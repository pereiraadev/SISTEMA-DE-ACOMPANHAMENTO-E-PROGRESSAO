package br.com.sistema.acompanhamento.historico;

import br.com.sistema.acompanhamento.historico.dto.HistoricoResposta;
import br.com.sistema.acompanhamento.historico.estrutura.CriterioOrdenacao;
import br.com.sistema.acompanhamento.historico.estrutura.DirecaoOrdenacao;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/exercicios", produces = MediaType.APPLICATION_JSON_VALUE)
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    // GET /api/exercicios/3/historico?ordenarPor=carga&direcao=desc
    // precisa do header Authorization: Bearer <token>
    @GetMapping("/{exercicioId}/historico")
    public HistoricoResposta historico(
            @PathVariable Long exercicioId,
            @RequestParam(defaultValue = "data") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direcao,
            Authentication authentication) {

        if (exercicioId == null || exercicioId <= 0) {
            throw new IllegalArgumentException("exercicioId deve ser maior que zero.");
        }
        if (authentication == null) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        // o email vem do token, entao cada um so ve o proprio historico
        return service.consultar(
                authentication.getName(),
                exercicioId,
                CriterioOrdenacao.de(ordenarPor),
                DirecaoOrdenacao.de(direcao));
    }
}
