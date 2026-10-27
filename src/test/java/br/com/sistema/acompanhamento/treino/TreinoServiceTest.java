package br.com.sistema.acompanhamento.treino;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TreinoServiceTest {
    @Mock
    private TreinoRepository repository;

    private TreinoService service;

    @BeforeEach
    void setUp() {
        service = new TreinoService(repository);
    }

    @Test
    void deveIniciarExecucaoClonandoModeloEItens() {
        Treino modelo = modeloComItem();
        when(repository.save(any(Treino.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Treino execucao = service.iniciarTreinoDeModelo(modelo);

        assertThat(execucao.isModelo()).isFalse();
        assertThat(execucao.getTreinoOrigem()).isSameAs(modelo);
        assertThat(execucao.getDataTreino()).isEqualTo(LocalDate.now());
        assertThat(execucao.getIniciadoEm()).isBetween(OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now().plusMinutes(1));
        assertThat(execucao.getStatus()).isEqualTo("EM_ANDAMENTO");
        assertThat(execucao.getItens()).hasSize(1);
        assertThat(execucao.getItens().get(0)).isNotSameAs(modelo.getItens().get(0));
        assertThat(execucao.getItens().get(0).getOrdem()).isEqualTo(1);
        assertThat(execucao.getItens().get(0).getCargaPrevista()).isEqualByComparingTo("20.00");
        verify(repository).save(execucao);
    }

    @Test
    void deveRecusarClonagemDeExecucao() {
        Treino execucao = new Treino();
        execucao.setModelo(false);

        assertThatThrownBy(() -> service.iniciarTreinoDeModelo(execucao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apenas modelos podem ser clonados");
    }

    @Test
    void deveTotalizarTreinosSeriesEVolumeApenasDeExecucoesConcluidas() {
        Treino concluido = execucao("CONCLUIDO", false, "50.00", 10, "50.00", 8);
        Treino emAndamento = execucao("EM_ANDAMENTO", false, "100.00", 10);
        Treino modelo = execucao("CONCLUIDO", true, "100.00", 10);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(concluido, emAndamento, modelo));

        EstatisticasGeraisDTO stats = service.consultarEstatisticasGerais(1L);

        assertThat(stats.totalTreinos()).isEqualTo(1);
        assertThat(stats.totalSeries()).isEqualTo(2);
        assertThat(stats.volumeTotal()).isEqualByComparingTo("900.00");
    }

    @Test
    void deveRetornarZerosQuandoNaoHaTreinos() {
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of());

        EstatisticasGeraisDTO stats = service.consultarEstatisticasGerais(1L);

        assertThat(stats.totalTreinos()).isZero();
        assertThat(stats.totalSeries()).isZero();
        assertThat(stats.volumeTotal()).isEqualByComparingTo("0");
    }

    // pares (carga, repeticoes) viram series de um unico item
    private Treino execucao(String status, boolean modelo, Object... cargaEReps) {
        Treino t = new Treino();
        t.setStatus(status);
        t.setModelo(modelo);
        ItemTreino item = new ItemTreino();
        t.adicionarItem(item);
        for (int i = 0; i < cargaEReps.length; i += 2) {
            SerieExecutada s = new SerieExecutada();
            s.setNumeroSerie(i / 2 + 1);
            s.setCargaUtilizada(new BigDecimal((String) cargaEReps[i]));
            s.setRepeticoesRealizadas((Integer) cargaEReps[i + 1]);
            item.adicionarSerie(s);
        }
        return t;
    }

    private Treino modeloComItem() {
        Treino modelo = new Treino();
        modelo.setModelo(true);
        modelo.setIdentificacao("Treino A");
        modelo.setStatus("ATIVO");

        ItemTreino item = new ItemTreino();
        item.setOrdem(1);
        item.setSeriesPrevistas(3);
        item.setRepsPrevistas(10);
        item.setCargaPrevista(new BigDecimal("20.00"));
        item.setConcluido(true);
        modelo.adicionarItem(item);
        return modelo;
    }
}
