package br.com.sistema.acompanhamento.treino;

import br.com.sistema.acompanhamento.comparativo.ComparativoDesempenho;
import br.com.sistema.acompanhamento.comparativo.DadosSerie;
import br.com.sistema.acompanhamento.historico.estrutura.CarregadorHistorico;
import br.com.sistema.acompanhamento.historico.estrutura.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.historico.estrutura.NoTreino;
import br.com.sistema.acompanhamento.historico.estrutura.SerieHistorico;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreinoService {
    private final TreinoRepository repository;
    private final ComparativoDesempenho comparativo = new ComparativoDesempenho();

    public TreinoService(TreinoRepository repository) { this.repository = repository; }

    @Transactional
    public Treino iniciarTreinoDeModelo(Treino modelo) {
        if (!modelo.isModelo()) throw new IllegalArgumentException("Apenas modelos podem ser clonados");
        Treino execucao = new Treino();
        execucao.setPraticante(modelo.getPraticante());
        execucao.setTreinoOrigem(modelo);
        execucao.setIdentificacao(modelo.getIdentificacao());
        execucao.setModelo(false);
        execucao.setDataTreino(LocalDate.now());
        execucao.setIniciadoEm(OffsetDateTime.now());
        execucao.setStatus("EM_ANDAMENTO");
        for (ItemTreino item : modelo.getItens()) {
            ItemTreino copia = new ItemTreino();
            copia.setExercicio(item.getExercicio());
            copia.setOrdem(item.getOrdem());
            copia.setSeriesPrevistas(item.getSeriesPrevistas());
            copia.setRepsPrevistas(item.getRepsPrevistas());
            copia.setCargaPrevista(item.getCargaPrevista());
            copia.setConcluido(false);
            execucao.adicionarItem(copia);
        }
        return repository.save(execucao);
    }

    // Totaliza treinos concluidos, series executadas e volume (carga x repeticoes) de um praticante.
    // Fichas modelo e treinos ainda em andamento ficam de fora, como no historico fechado.
    @Transactional(readOnly = true)
    public EstatisticasGeraisDTO consultarEstatisticasGerais(Long praticanteId) {
        long totalTreinos = 0;
        long totalSeries = 0;
        BigDecimal volumeTotal = BigDecimal.ZERO;

        for (Treino treino : repository.findByPraticanteIdOrderByIdDesc(praticanteId)) {
            if (treino.isModelo() || !"CONCLUIDO".equals(treino.getStatus()) || treino.getFinalizadoEm() == null) continue;
            totalTreinos++;
            for (ItemTreino item : treino.getItens()) {
                for (SerieExecutada serie : item.getSeries()) {
                    totalSeries++;
                    volumeTotal = volumeTotal.add(
                            serie.getCargaUtilizada().multiply(BigDecimal.valueOf(serie.getRepeticoesRealizadas())));
                }
            }
        }
        return new EstatisticasGeraisDTO(totalTreinos, totalSeries, volumeTotal);
    }

    // Monta o historico (lista duplamente encadeada) com os treinos concluidos do praticante
    // e devolve os treinos ordenados em memoria pelo criterio pedido.
    // DATA sai da mais antiga para a mais recente; CARGA, REPETICOES e VOLUME saem do maior para o menor.
    @Transactional(readOnly = true)
    public List<HistoricoTreinoDTO> consultarHistoricoOrdenado(Long praticanteId, CriterioOrdenacao criterio) {
        ListaDuplaTreinos<HistoricoTreinoAdapter> historico = carregarHistorico(praticanteId);

        List<HistoricoTreinoAdapter> ordenados = switch (criterio) {
            case DATA -> historico.ordenarPorData();
            case CARGA -> historico.ordenarPorCarga();
            case REPETICOES -> historico.ordenarPorRepeticoes();
            case VOLUME -> historico.ordenarPorVolume();
        };
        return ordenados.stream().map(TreinoService::paraDto).toList();
    }

    // Compara o exercicio entre o treino concluido informado e o treino anterior que tambem o continha.
    // Em cada treino vale a melhor serie (maior carga; empate decide por repeticoes), como em ComparativoDesempenho.
    // Sem treino anterior com o exercicio (ou sem series dele no treino atual), o resultado e SEM_DADOS.
    @Transactional(readOnly = true)
    public ComparativoDesempenhoDTO consultarComparativoDesempenho(Long praticanteId, Long treinoId, Long exercicioId) {
        ListaDuplaTreinos<HistoricoTreinoAdapter> historico = carregarHistorico(praticanteId);

        HistoricoTreinoAdapter atual = null;
        for (NoTreino<HistoricoTreinoAdapter> no = historico.getInicio(); no != null && atual == null; no = no.getProximo()) {
            if (Objects.equals(treinoId, no.getTreino().getId())) atual = no.getTreino();
        }
        if (atual == null) throw new IllegalArgumentException("Treino concluido nao encontrado para o praticante");

        HistoricoTreinoAdapter anterior = historico.obterTreinoAnteriorPorExercicio(atual, exercicioId);

        ArrayList<DadosSerie> seriesAtuais = seriesDoExercicio(atual, exercicioId);
        ArrayList<DadosSerie> seriesAnteriores = anterior == null ? new ArrayList<>() : seriesDoExercicio(anterior, exercicioId);

        String resultado = comparativo.compararExercicios(seriesAnteriores, seriesAtuais);
        DadosSerie melhorAtual = comparativo.buscarMelhorSerie(seriesAtuais);
        DadosSerie melhorAnterior = comparativo.buscarMelhorSerie(seriesAnteriores);

        boolean comparavel = melhorAtual != null && melhorAnterior != null;
        return new ComparativoDesempenhoDTO(
                exercicioId,
                atual.getId(),
                anterior == null ? null : anterior.getId(),
                resultado,
                melhorAnterior == null ? null : melhorAnterior.getCarga(),
                melhorAnterior == null ? null : melhorAnterior.getRepeticoes(),
                melhorAtual == null ? null : melhorAtual.getCarga(),
                melhorAtual == null ? null : melhorAtual.getRepeticoes(),
                comparavel ? melhorAtual.getCarga() - melhorAnterior.getCarga() : null,
                comparavel ? melhorAtual.getRepeticoes() - melhorAnterior.getRepeticoes() : null);
    }

    // Treinos concluidos do praticante (sem modelos nem em andamento) carregados na lista duplamente encadeada.
    // O carregador ordena por finalizacao em UTC e desempata pelo ID.
    private ListaDuplaTreinos<HistoricoTreinoAdapter> carregarHistorico(Long praticanteId) {
        List<HistoricoTreinoAdapter> concluidos = new ArrayList<>(
                repository.findByPraticanteIdOrderByIdDesc(praticanteId).stream()
                        .filter(t -> !t.isModelo() && "CONCLUIDO".equals(t.getStatus()) && t.getFinalizadoEm() != null)
                        .map(HistoricoTreinoAdapter::new)
                        .toList());
        return new CarregadorHistorico<HistoricoTreinoAdapter>().carregar(concluidos);
    }

    private static ArrayList<DadosSerie> seriesDoExercicio(HistoricoTreinoAdapter treino, Long exercicioId) {
        ArrayList<DadosSerie> series = new ArrayList<>();
        for (SerieHistorico serie : treino.getSeriesExecutadas()) {
            if (Objects.equals(exercicioId, serie.getExercicioId())) {
                series.add(new DadosSerie(serie.getCarga(), serie.getRepeticoes()));
            }
        }
        return series;
    }

    private static HistoricoTreinoDTO paraDto(HistoricoTreinoAdapter adapter) {
        double maiorCarga = 0;
        int maiorRepeticoes = 0;
        double volume = 0;
        var series = adapter.getSeriesExecutadas();
        for (var serie : series) {
            maiorCarga = Math.max(maiorCarga, serie.getCarga());
            maiorRepeticoes = Math.max(maiorRepeticoes, serie.getRepeticoes());
            volume += serie.getCarga() * serie.getRepeticoes();
        }
        return new HistoricoTreinoDTO(adapter.getId(), adapter.getTreino().getIdentificacao(),
                adapter.getDataRealizacao(), series.size(), maiorCarga, maiorRepeticoes, volume);
    }

    @Transactional
    public Treino clonarParaExecucao(Treino modelo) {
        return iniciarTreinoDeModelo(modelo);
    }
}
