package br.com.sistema.acompanhamento.dashboard;

import br.com.sistema.acompanhamento.dashboard.dto.DashboardResposta;
import br.com.sistema.acompanhamento.historico.estrutura.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.praticante.Praticante;
import br.com.sistema.acompanhamento.praticante.PraticanteRepository;
import br.com.sistema.acompanhamento.treino.TreinoRepository;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final PraticanteRepository praticanteRepository;
    private final TreinoRepository treinoRepository;

    public DashboardService(PraticanteRepository praticanteRepository, TreinoRepository treinoRepository) {
        this.praticanteRepository = praticanteRepository;
        this.treinoRepository = treinoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResposta montar(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Praticante não encontrado."));

        ListaDuplaTreinos lista = ListaDuplaTreinos.de(
                treinoRepository.buscarExecucoesConcluidas(praticante.getId()));

        return DashboardCalculador.calcular(lista, LocalDate.now());
    }
}
