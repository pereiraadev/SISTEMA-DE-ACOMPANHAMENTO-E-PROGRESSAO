package br.com.sistema.acompanhamento.comparativo;

import br.com.sistema.acompanhamento.comparativo.dto.ComparativoResposta;
import br.com.sistema.acompanhamento.comparativo.dto.ResumoSessao;
import br.com.sistema.acompanhamento.historico.estrutura.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.praticante.Praticante;
import br.com.sistema.acompanhamento.praticante.PraticanteRepository;
import br.com.sistema.acompanhamento.treino.ItemTreino;
import br.com.sistema.acompanhamento.treino.Treino;
import br.com.sistema.acompanhamento.treino.TreinoRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComparativoService {

    private final PraticanteRepository praticanteRepository;
    private final TreinoRepository treinoRepository;

    public ComparativoService(PraticanteRepository praticanteRepository, TreinoRepository treinoRepository) {
        this.praticanteRepository = praticanteRepository;
        this.treinoRepository = treinoRepository;
    }

    // @Transactional pelo mesmo motivo do historico (itens e series sao lazy)
    @Transactional(readOnly = true)
    public ComparativoResposta comparar(String email, Long treinoId, Long exercicioId) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Praticante não encontrado."));

        // ja filtra pelo dono do treino, se for de outro usuario volta 404
        Treino atual = treinoRepository.findByIdAndPraticanteId(treinoId, praticante.getId())
                .orElseThrow(() -> new NoSuchElementException("Treino não encontrado."));

        if (atual.isModelo()) {
            throw new IllegalArgumentException("Treino modelo não tem execução para comparar.");
        }

        ListaDuplaTreinos lista = ListaDuplaTreinos.de(
                treinoRepository.buscarExecucoesConcluidas(praticante.getId()));

        ItemTreino itemAtual = lista.obterItemPorExercicio(atual, exercicioId);
        if (itemAtual == null) {
            throw new NoSuchElementException("Esse exercício não faz parte do treino.");
        }

        // treino concluido: anterior e o que veio antes dele na lista
        // treino em andamento: ainda nao esta na lista, entao o anterior e o ultimo concluido
        Treino treinoAnterior;
        if ("CONCLUIDO".equals(atual.getStatus())) {
            treinoAnterior = lista.obterTreinoAnteriorPorExercicio(atual, exercicioId);
        } else {
            treinoAnterior = lista.obterUltimoTreinoPorExercicio(exercicioId);
        }

        ResumoSessao resumoAtual = ComparativoCalculador.resumir(atual, itemAtual);

        if (treinoAnterior == null) {
            // primeira vez que o exercicio aparece, nao tem o que comparar
            return new ComparativoResposta(treinoId, exercicioId,
                    itemAtual.getExercicio().getNome(), resumoAtual, null, null);
        }

        ItemTreino itemAnterior = lista.obterItemPorExercicio(treinoAnterior, exercicioId);
        ResumoSessao resumoAnterior = ComparativoCalculador.resumir(treinoAnterior, itemAnterior);

        return new ComparativoResposta(treinoId, exercicioId, itemAtual.getExercicio().getNome(),
                resumoAtual, resumoAnterior, ComparativoCalculador.comparar(resumoAtual, resumoAnterior));
    }
}
