package br.com.sistema.acompanhamento.treino.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.sistema.acompanhamento.exercicio.domain.Exercicio;
import br.com.sistema.acompanhamento.treino.api.dto.ComparativoDesempenhoDTO;
import br.com.sistema.acompanhamento.treino.api.dto.EstatisticasGeraisDTO;
import br.com.sistema.acompanhamento.treino.api.dto.HistoricoTreinoDTO;
import br.com.sistema.acompanhamento.treino.domain.CriterioOrdenacao;
import br.com.sistema.acompanhamento.treino.domain.ItemTreino;
import br.com.sistema.acompanhamento.treino.domain.SerieExecutada;
import br.com.sistema.acompanhamento.treino.domain.Treino;
import br.com.sistema.acompanhamento.treino.repository.TreinoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    void deveOrdenarHistoricoPorCadaCriterioIgnorandoModelosEAndamento() {
        Treino a = execucaoEm("A", LocalDate.of(2026, 10, 10), "80.00", 10, "80.00", 10); // vol 1600, carga 80, reps 10
        Treino b = execucaoEm("B", LocalDate.of(2026, 10, 12), "60.00", 20, "60.00", 20); // vol 2400, carga 60, reps 20
        Treino c = execucaoEm("C", LocalDate.of(2026, 10, 15), "90.00", 5, "90.00", 5);   // vol 900, carga 90, reps 5
        Treino andamento = execucaoEm("ANDAMENTO", LocalDate.of(2026, 10, 16), "200.00", 50);
        andamento.setStatus("EM_ANDAMENTO");
        Treino modelo = execucaoEm("MODELO", LocalDate.of(2026, 10, 1), "300.00", 50);
        modelo.setModelo(true);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(c, andamento, modelo, b, a));

        assertThat(service.consultarHistoricoOrdenado(1L, CriterioOrdenacao.DATA))
                .extracting(HistoricoTreinoDTO::identificacao).containsExactly("A", "B", "C");
        assertThat(service.consultarHistoricoOrdenado(1L, CriterioOrdenacao.CARGA))
                .extracting(HistoricoTreinoDTO::identificacao).containsExactly("C", "A", "B");
        assertThat(service.consultarHistoricoOrdenado(1L, CriterioOrdenacao.REPETICOES))
                .extracting(HistoricoTreinoDTO::identificacao).containsExactly("B", "A", "C");
        assertThat(service.consultarHistoricoOrdenado(1L, CriterioOrdenacao.VOLUME))
                .extracting(HistoricoTreinoDTO::identificacao).containsExactly("B", "A", "C");
    }

    @Test
    void deveResumirTreinoNoHistorico() {
        Treino a = execucaoEm("A", LocalDate.of(2026, 10, 10), "80.00", 10, "70.00", 12);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(a));

        HistoricoTreinoDTO dto = service.consultarHistoricoOrdenado(1L, CriterioOrdenacao.DATA).get(0);

        assertThat(dto.totalSeries()).isEqualTo(2);
        assertThat(dto.maiorCarga()).isEqualTo(80.0);
        assertThat(dto.maiorRepeticoes()).isEqualTo(12);
        assertThat(dto.volumeTotal()).isEqualTo(1640.0);
        assertThat(dto.dataRealizacao()).isEqualTo(LocalDate.of(2026, 10, 10).atStartOfDay());
    }

    @Test
    void deveConverterCriterioDeOrdenacao() {
        assertThat(CriterioOrdenacao.de(null)).isEqualTo(CriterioOrdenacao.DATA);
        assertThat(CriterioOrdenacao.de("Carga")).isEqualTo(CriterioOrdenacao.CARGA);
        assertThatThrownBy(() -> CriterioOrdenacao.de("peso"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("peso");
    }

    @Test
    void deveIndicarAumentoDeCargaPulandoTreinoSemOExercicio() {
        Exercicio supino = exercicio(5L);
        Exercicio remada = exercicio(6L);
        Treino t1 = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "80.00", 8, "70.00", 10);
        Treino t2 = execucaoComExercicio(2L, LocalDate.of(2026, 10, 3), remada, "50.00", 12);
        Treino t3 = execucaoComExercicio(3L, LocalDate.of(2026, 10, 5), supino, "85.00", 6);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t3, t2, t1));

        ComparativoDesempenhoDTO dto = service.consultarComparativoDesempenho(1L, 3L, 5L);

        assertThat(dto.resultado()).isEqualTo("AUMENTO_CARGA");
        assertThat(dto.treinoAtualId()).isEqualTo(3L);
        assertThat(dto.treinoAnteriorId()).isEqualTo(1L);
        assertThat(dto.cargaAnterior()).isEqualTo(80.0);
        assertThat(dto.repeticoesAnteriores()).isEqualTo(8);
        assertThat(dto.cargaAtual()).isEqualTo(85.0);
        assertThat(dto.repeticoesAtuais()).isEqualTo(6);
        assertThat(dto.diferencaCarga()).isEqualTo(5.0);
        assertThat(dto.diferencaRepeticoes()).isEqualTo(-2);
    }

    @Test
    void deveIndicarReducaoDeCarga() {
        Exercicio supino = exercicio(5L);
        Treino t1 = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "85.00", 8);
        Treino t2 = execucaoComExercicio(2L, LocalDate.of(2026, 10, 3), supino, "80.00", 10);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t2, t1));

        ComparativoDesempenhoDTO dto = service.consultarComparativoDesempenho(1L, 2L, 5L);

        assertThat(dto.resultado()).isEqualTo("REDUCAO_CARGA");
        assertThat(dto.diferencaCarga()).isEqualTo(-5.0);
    }

    @Test
    void deveIndicarEvolucaoEReducaoPorRepeticoesComMesmaCarga() {
        Exercicio supino = exercicio(5L);
        Treino t1 = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "85.00", 6);
        Treino t2 = execucaoComExercicio(2L, LocalDate.of(2026, 10, 3), supino, "85.00", 8);
        Treino t3 = execucaoComExercicio(3L, LocalDate.of(2026, 10, 5), supino, "85.00", 7);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t3, t2, t1));

        assertThat(service.consultarComparativoDesempenho(1L, 2L, 5L).resultado()).isEqualTo("AUMENTO_REPETICOES");
        assertThat(service.consultarComparativoDesempenho(1L, 3L, 5L).resultado()).isEqualTo("REDUCAO_REPETICOES");
    }

    @Test
    void deveIndicarManutencaoQuandoNadaMudou() {
        Exercicio supino = exercicio(5L);
        Treino t1 = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "85.00", 8);
        Treino t2 = execucaoComExercicio(2L, LocalDate.of(2026, 10, 3), supino, "85.00", 8);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t2, t1));

        ComparativoDesempenhoDTO dto = service.consultarComparativoDesempenho(1L, 2L, 5L);

        assertThat(dto.resultado()).isEqualTo("MANUTENCAO");
        assertThat(dto.diferencaCarga()).isEqualTo(0.0);
        assertThat(dto.diferencaRepeticoes()).isEqualTo(0);
    }

    @Test
    void deveRetornarSemDadosNaPrimeiraVezDoExercicio() {
        Exercicio supino = exercicio(5L);
        Treino t1 = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "80.00", 8);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t1));

        ComparativoDesempenhoDTO dto = service.consultarComparativoDesempenho(1L, 1L, 5L);

        assertThat(dto.resultado()).isEqualTo("SEM_DADOS");
        assertThat(dto.treinoAnteriorId()).isNull();
        assertThat(dto.cargaAnterior()).isNull();
        assertThat(dto.cargaAtual()).isEqualTo(80.0);
        assertThat(dto.diferencaCarga()).isNull();
    }

    @Test
    void deveUsarOTreinoMaisNovoComoAtualQuandoDatasSaoIguais() {
        Exercicio supino = exercicio(5L);
        Treino t10 = execucaoComExercicio(10L, LocalDate.of(2026, 11, 1), supino, "60.00", 10);
        Treino t11 = execucaoComExercicio(11L, LocalDate.of(2026, 11, 1), supino, "65.00", 10);
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(t11, t10));

        ComparativoDesempenhoDTO dto = service.consultarComparativoDesempenho(1L, 11L, 5L);

        assertThat(dto.treinoAnteriorId()).isEqualTo(10L);
        assertThat(dto.resultado()).isEqualTo("AUMENTO_CARGA");
    }

    @Test
    void deveRecusarComparativoDeTreinoInexistenteOuNaoConcluido() {
        Exercicio supino = exercicio(5L);
        Treino concluido = execucaoComExercicio(1L, LocalDate.of(2026, 10, 1), supino, "80.00", 8);
        Treino andamento = execucaoComExercicio(2L, LocalDate.of(2026, 10, 3), supino, "85.00", 8);
        andamento.setStatus("EM_ANDAMENTO");
        when(repository.findByPraticanteIdOrderByIdDesc(1L)).thenReturn(List.of(andamento, concluido));

        assertThatThrownBy(() -> service.consultarComparativoDesempenho(1L, 2L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Treino concluido nao encontrado para o praticante");
        assertThatThrownBy(() -> service.consultarComparativoDesempenho(1L, 99L, 5L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Exercicio exercicio(Long id) {
        Exercicio exercicio = new Exercicio();
        ReflectionTestUtils.setField(exercicio, "id", id);
        return exercicio;
    }

    // o id do Treino e gerado pelo banco e nao tem setter, entao entra por reflexao
    private Treino execucaoComExercicio(Long id, LocalDate data, Exercicio exercicio, Object... cargaEReps) {
        Treino t = execucaoEm("T" + id, data, cargaEReps);
        ReflectionTestUtils.setField(t, "id", id);
        t.getItens().get(0).setExercicio(exercicio);
        return t;
    }

    private Treino execucaoEm(String identificacao, LocalDate data, Object... cargaEReps) {
        Treino t = execucao("CONCLUIDO", false, cargaEReps);
        t.setIdentificacao(identificacao);
        t.setDataTreino(data);
        return t;
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
