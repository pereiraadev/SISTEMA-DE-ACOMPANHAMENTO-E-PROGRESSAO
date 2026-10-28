package br.com.sistema.acompanhamento.service;

import br.com.sistema.acompanhamento.CriterioOrdenacao;
import br.com.sistema.acompanhamento.DirecaoOrdenacao;
import br.com.sistema.acompanhamento.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.Praticante;
import br.com.sistema.acompanhamento.dto.HistoricoDTO;
import br.com.sistema.acompanhamento.dto.RegistroSerieDTO;
import br.com.sistema.acompanhamento.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.repository.TreinoRepository;

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

    // Precisa do @Transactional porque o open-in-view esta desligado no application.yml,
    // Senao os itens e series (lazy) dao erro quando a gente le fora daqui.
    @Transactional(readOnly = true)
    public HistoricoDTO consultar(String email, Long exercicioId,
                                       CriterioOrdenacao criterio, DirecaoOrdenacao direcao) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        // O token tem o email, entao acha o praticante por ele.
        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Praticante não encontrado."));

        ListaDuplaTreinos lista = ListaDuplaTreinos.de(
                treinoRepository.buscarExecucoesConcluidas(praticante.getId()));

        List<RegistroSerieDTO> series = lista.historicoPorExercicio(exercicioId);
        List<RegistroSerieDTO> ordenado = lista.ordenarHistorico(series, criterio, direcao.decrescente());

        return new HistoricoDTO(
                exercicioId, criterio.comoTexto(), direcao.comoTexto(), ordenado.size(), ordenado);
    }
}
