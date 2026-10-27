package br.com.sistema.acompanhamento.treino;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TreinoService {
    private final TreinoRepository repository;

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
            if (treino.isModelo() || !"CONCLUIDO".equals(treino.getStatus())) continue;
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

    @Transactional
    public Treino clonarParaExecucao(Treino modelo) {
        return iniciarTreinoDeModelo(modelo);
    }
}
