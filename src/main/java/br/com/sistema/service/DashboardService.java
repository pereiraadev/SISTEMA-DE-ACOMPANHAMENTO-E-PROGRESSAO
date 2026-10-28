package br.com.sistema.acompanhamento.service;

import br.com.sistema.acompanhamento.DashboardCalculador;
import br.com.sistema.acompanhamento.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.Praticante;
import br.com.sistema.acompanhamento.dto.DashboardDTO;
import br.com.sistema.acompanhamento.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.repository.TreinoRepository;

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
    public DashboardDTO montar(String email) {
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
