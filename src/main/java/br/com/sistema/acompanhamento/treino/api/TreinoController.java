package br.com.sistema.acompanhamento.treino.api;

import br.com.sistema.acompanhamento.praticante.domain.Praticante;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.treino.service.TreinoService;
import br.com.sistema.acompanhamento.treino.api.dto.HistoricoTreinoDTO;
import br.com.sistema.acompanhamento.treino.domain.CriterioOrdenacao;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/treinos")
public class TreinoController {
    private final TreinoService service;
    private final PraticanteRepository praticanteRepository;

    public TreinoController(TreinoService service, PraticanteRepository praticanteRepository) {
        this.service = service;
        this.praticanteRepository = praticanteRepository;
    }

    // GET /treinos/historico?ordem=data|carga|repeticoes|volume (padrao: data)
    // O praticante vem do token JWT: o SecurityFilter coloca o e-mail como nome da autenticacao.
    @GetMapping("/historico")
    public List<HistoricoTreinoDTO> historico(
            @RequestParam(name = "ordem", defaultValue = "data") String ordem,
            Authentication authentication) {
        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Praticante nao encontrado"));
        CriterioOrdenacao criterio;
        try {
            criterio = CriterioOrdenacao.de(ordem);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return service.consultarHistoricoOrdenado(praticante.getId(), criterio);
    }
}
