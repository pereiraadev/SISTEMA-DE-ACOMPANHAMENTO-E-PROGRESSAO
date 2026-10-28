package br.com.sistema.acompanhamento.historico;

import br.com.sistema.acompanhamento.historico.dto.HistoricoResposta;
import br.com.sistema.acompanhamento.historico.dto.RegistroSerie;
import br.com.sistema.acompanhamento.historico.estrutura.CriterioOrdenacao;
import br.com.sistema.acompanhamento.historico.estrutura.DirecaoOrdenacao;
import br.com.sistema.acompanhamento.historico.estrutura.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.praticante.Praticante;
import br.com.sistema.acompanhamento.praticante.PraticanteRepository;
import br.com.sistema.acompanhamento.treino.TreinoRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricoService {

    private final PraticanteRepository praticanteRepository;
    private final TreinoRepository treinoRepository;

    public HistoricoService(PraticanteRepository praticanteRepository, TreinoRepository treinoRepository) {
        this.praticanteRepository = praticanteRepository;
        this.treinoRepository = treinoRepository;
    }

    // precisa do @Transactional porque o open-in-view esta desligado no application.yml,
    // senao os itens e series (lazy) dao erro quando a gente le fora daqui
    @Transactional(readOnly = true)
    public HistoricoResposta consultar(String email, Long exercicioId,
                                       CriterioOrdenacao criterio, DirecaoOrdenacao direcao) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        // o token tem o email, entao acha o praticante por ele
        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Praticante não encontrado."));

        ListaDuplaTreinos lista = ListaDuplaTreinos.de(
                treinoRepository.buscarExecucoesConcluidas(praticante.getId()));

        List<RegistroSerie> series = lista.historicoPorExercicio(exercicioId);
        List<RegistroSerie> ordenado = lista.ordenarHistorico(series, criterio, direcao.decrescente());

        return new HistoricoResposta(
                exercicioId, criterio.comoTexto(), direcao.comoTexto(), ordenado.size(), ordenado);
    }
}
