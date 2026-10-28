package br.com.sistema.acompanhamento.service;

import br.com.sistema.acompanhamento.ComparativoDesempenho;
import br.com.sistema.acompanhamento.DadosSerie;
import br.com.sistema.acompanhamento.ItemTreino;
import br.com.sistema.acompanhamento.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.Praticante;
import br.com.sistema.acompanhamento.SerieExecutada;
import br.com.sistema.acompanhamento.Treino;
import br.com.sistema.acompanhamento.dto.ComparativoDTO;
import br.com.sistema.acompanhamento.dto.ResumoSessaoDTO;
import br.com.sistema.acompanhamento.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.repository.TreinoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComparativoService {

    private final PraticanteRepository praticanteRepository;
    private final TreinoRepository treinoRepository;

    // Classe do comparativo que ja existia, compara as melhores series de dois treinos.
    private final ComparativoDesempenho comparativo = new ComparativoDesempenho();

    public ComparativoService(PraticanteRepository praticanteRepository, TreinoRepository treinoRepository) {
        this.praticanteRepository = praticanteRepository;
        this.treinoRepository = treinoRepository;
    }

    // Precisa do @Transactional porque itens e series sao lazy e o open-in-view esta desligado.
    @Transactional(readOnly = true)
    public ComparativoDTO comparar(String email, Long treinoId, Long exercicioId) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usuário não identificado.");
        }

        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("Praticante não encontrado."));

        // Ja filtra pelo dono do treino, se for de outro usuario volta 404.
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

        // Treino concluido: o anterior e o que veio antes dele na lista.
        // Treino em andamento: ainda nao esta na lista, entao o anterior e o ultimo concluido.
        Treino treinoAnterior;
        if ("CONCLUIDO".equals(atual.getStatus())) {
            treinoAnterior = lista.obterTreinoAnteriorPorExercicio(atual, exercicioId);
        } else {
            treinoAnterior = lista.obterUltimoTreinoPorExercicio(exercicioId);
        }

        ResumoSessaoDTO resumoAtual = resumir(atual, itemAtual);
        ArrayList<DadosSerie> seriesAtuais = converterSeries(itemAtual);

        // Sem treino anterior a lista fica vazia e o resultado sai como SEM_DADOS.
        ArrayList<DadosSerie> seriesAnteriores = new ArrayList<>();
        ResumoSessaoDTO resumoAnterior = null;
        BigDecimal diferencaVolume = null;

        if (treinoAnterior != null) {
            ItemTreino itemAnterior = lista.obterItemPorExercicio(treinoAnterior, exercicioId);
            seriesAnteriores = converterSeries(itemAnterior);
            resumoAnterior = resumir(treinoAnterior, itemAnterior);
            diferencaVolume = resumoAtual.volumeTotal().subtract(resumoAnterior.volumeTotal());
        }

        String resultado = comparativo.compararExercicios(seriesAnteriores, seriesAtuais);

        return new ComparativoDTO(treinoId, exercicioId, itemAtual.getExercicio().getNome(),
                resultado, resumoAtual, resumoAnterior, diferencaVolume);
    }

    // Transforma as series do banco no formato que o ComparativoDesempenho recebe.
    private ArrayList<DadosSerie> converterSeries(ItemTreino item) {
        ArrayList<DadosSerie> series = new ArrayList<>();
        for (SerieExecutada serie : item.getSeries()) {
            series.add(new DadosSerie(serie.getCargaUtilizada().doubleValue(), serie.getRepeticoesRealizadas()));
        }
        return series;
    }

    private ResumoSessaoDTO resumir(Treino treino, ItemTreino item) {
        return new ResumoSessaoDTO(
                treino.getId(),
                treino.getDataTreino(),
                item.obterMaiorCarga(),
                item.contarRepeticoes(),
                item.getSeries().size(),
                item.calcularVolumeTotal());
    }
}
