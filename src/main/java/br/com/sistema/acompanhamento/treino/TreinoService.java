package br.com.sistema.acompanhamento.treino;

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

    @Transactional
    public Treino clonarParaExecucao(Treino modelo) {
        return iniciarTreinoDeModelo(modelo);
    }
}
