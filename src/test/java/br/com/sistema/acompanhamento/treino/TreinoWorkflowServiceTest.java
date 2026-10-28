package br.com.sistema.acompanhamento.treino;

import br.com.sistema.acompanhamento.api.*;
import br.com.sistema.acompanhamento.exercicio.*;
import br.com.sistema.acompanhamento.praticante.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreinoWorkflowServiceTest {
    @Mock PraticanteRepository praticantes;
    @Mock TreinoRepository treinos;
    @Mock SerieExecutadaRepository series;
    @Mock ExercicioRepository exercicios;
    TreinoService clonador;
    TreinoWorkflowService service;
    Praticante pessoa;

    @BeforeEach void iniciar() {
        clonador = new TreinoService(treinos);
        service = new TreinoWorkflowService(praticantes, treinos, series, exercicios, clonador);
        pessoa = new Praticante(); ReflectionTestUtils.setField(pessoa, "id", 1L);
        when(praticantes.findByEmailIgnoreCase("a@teste.com")).thenReturn(Optional.of(pessoa));
    }
    private Exercicio exercicio(long id) {
        Exercicio e = new Exercicio(); ReflectionTestUtils.setField(e, "id", id);
        e.setNome("Supino"); e.setGrupoMuscular("PEITO"); e.setPraticante(pessoa);
        return e;
    }
    private Treino treino(long id, boolean modelo, String status, Praticante dono, Exercicio e) {
        Treino t = new Treino(); ReflectionTestUtils.setField(t, "id", id);
        t.setPraticante(dono); t.setModelo(modelo); t.setStatus(status); t.setIdentificacao("A");
        ItemTreino i = new ItemTreino(); ReflectionTestUtils.setField(i, "id", id * 10);
        i.setExercicio(e); i.setOrdem(1); i.setSeriesPrevistas(2); i.setRepsPrevistas(10);
        t.adicionarItem(i);
        return t;
    }
    private SerieExecutada serie(ItemTreino item, long id, int numero, String carga, int reps) {
        SerieExecutada s = new SerieExecutada(); ReflectionTestUtils.setField(s, "id", id);
        s.setItemTreino(item); s.setNumeroSerie(numero); s.setCargaUtilizada(new BigDecimal(carga));
        s.setRepeticoesRealizadas(reps); return s;
    }

    @Test void bloqueiaTreinoDeOutroPraticante() {
        Praticante outro = new Praticante(); ReflectionTestUtils.setField(outro, "id", 2L);
        when(treinos.findById(3L)).thenReturn(Optional.of(treino(3, false, "EM_ANDAMENTO", outro, exercicio(1))));
        assertEquals("NAO_ENCONTRADO", assertThrows(ApiException.class,
                () -> service.execucao("a@teste.com", 3L)).getCodigo());
    }
    @Test void modeloExigeExercicioDoProprioPraticanteEOrdemUnica() {
        Exercicio e = exercicio(7);
        when(exercicios.findById(7L)).thenReturn(Optional.of(e));
        ApiDtos.ItemModelo item = new ApiDtos.ItemModelo(7L, 1, 2, 10, BigDecimal.TEN);
        assertEquals("ESTADO_INVALIDO", assertThrows(ApiException.class, () -> service.criarModelo("a@teste.com",
                new ApiDtos.NovoModelo("A", List.of(item, item)))).getCodigo());
        verify(treinos, never()).saveAndFlush(any());
    }
    @Test void clonaApenasModeloDoDono() {
        Treino modelo = treino(2, true, "MODELO", pessoa, exercicio(7));
        when(treinos.findById(2L)).thenReturn(Optional.of(modelo));
        when(treinos.save(any(Treino.class))).thenAnswer(i -> {
            Treino salvo = i.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 3L);
            ReflectionTestUtils.setField(salvo.getItens().get(0), "id", 30L);
            return salvo;
        });
        ApiDtos.TreinoResposta resposta = service.iniciar("a@teste.com", 2L);
        assertEquals(3L, resposta.id());
        assertNotNull(resposta.iniciadoEm());
        assertEquals(1, resposta.itens().size());
    }
    @Test void registraSerieERejeitaNumeroDuplicado() {
        Treino t = treino(3, false, "EM_ANDAMENTO", pessoa, exercicio(7));
        when(treinos.findLockedById(3L)).thenReturn(Optional.of(t));
        when(series.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        ApiDtos.SerieEntrada entrada = new ApiDtos.SerieEntrada(1, new BigDecimal("40.50"), 10);
        assertEquals(new BigDecimal("405.00"), service.registrarSerie("a@teste.com", 3L, 30L, entrada).volume());
        when(series.existsByItemTreinoIdAndNumeroSerie(30L, 1)).thenReturn(true);
        assertEquals("ESTADO_INVALIDO", assertThrows(ApiException.class,
                () -> service.registrarSerie("a@teste.com", 3L, 30L, entrada)).getCodigo());
    }
    @Test void finalizaUmaVezEAtualizaRecordesDoExercicioProprio() {
        Exercicio e = exercicio(7);
        Treino t = treino(3, false, "EM_ANDAMENTO", pessoa, e);
        when(treinos.findLockedById(3L)).thenReturn(Optional.of(t));
        when(series.findForTreinos(List.of(3L))).thenReturn(List.of(
                serie(t.getItens().get(0), 1, 1, "40.00", 10),
                serie(t.getItens().get(0), 2, 2, "40.00", 8)));
        assertEquals(new BigDecimal("720.00"), service.finalizar("a@teste.com", 3L).volume());
        assertEquals("CONCLUIDO", t.getStatus());
        assertNotNull(t.getFinalizadoEm());
        assertEquals(new BigDecimal("40.00"), e.getRecordeCarga());
        assertEquals(10, e.getRecordeRepsParaCarga());
        assertEquals(new BigDecimal("720.00"), e.getRecordeVolumeSessao());
        assertEquals(1, e.getTotalVezesRealizado());
        assertThrows(ApiException.class, () -> service.finalizar("a@teste.com", 3L));
    }
    @Test void historicoIgnoraModeloEAndamentoEOrdenaPorHorario() {
        Exercicio e = exercicio(7);
        Treino antigo = treino(4, false, "CONCLUIDO", pessoa, e);
        antigo.setFinalizadoEm(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        Treino recente = treino(5, false, "CONCLUIDO", pessoa, e);
        recente.setFinalizadoEm(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        Treino aberto = treino(6, false, "EM_ANDAMENTO", pessoa, e);
        when(treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(1L))
                .thenReturn(List.of(aberto, recente, antigo));
        when(series.findForTreinos(List.of(5L, 4L))).thenReturn(List.of());
        List<ApiDtos.TreinoResposta> historico = service.historico("a@teste.com");
        assertEquals(List.of(4L, 5L), historico.stream().map(ApiDtos.TreinoResposta::id).toList());
    }
    @Test void calculaRecordePessoalDeSeriesConcluidas() {
        Exercicio e = exercicio(7);
        Treino t = treino(3, false, "CONCLUIDO", pessoa, e);
        t.setFinalizadoEm(OffsetDateTime.now());
        when(exercicios.findById(7L)).thenReturn(Optional.of(e));
        when(treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(1L)).thenReturn(List.of(t));
        when(series.findForTreinos(List.of(3L))).thenReturn(List.of(
                serie(t.getItens().get(0), 1, 1, "40.00", 10),
                serie(t.getItens().get(0), 2, 2, "40.00", 12)));
        ApiDtos.RecordeResposta recorde = service.recordes("a@teste.com", 7L);
        assertEquals(12, recorde.repeticoesNaMaiorCarga());
        assertEquals(new BigDecimal("880.00"), recorde.maiorVolumeSessao());
        assertEquals(1, recorde.sessoesRealizadas());
    }
    @Test void encontraSessaoAnteriorPeloPonteiroEVolumeDoExercicio() {
        Exercicio e = exercicio(7);
        Treino antigo = treino(3, false, "CONCLUIDO", pessoa, e);
        antigo.setFinalizadoEm(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        Treino recente = treino(4, false, "CONCLUIDO", pessoa, e);
        recente.setFinalizadoEm(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        when(exercicios.findById(7L)).thenReturn(Optional.of(e));
        when(treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(1L)).thenReturn(List.of(recente, antigo));
        when(series.findForTreinos(List.of(4L, 3L))).thenReturn(List.of(
                serie(antigo.getItens().get(0), 1, 1, "40.00", 10),
                serie(recente.getItens().get(0), 2, 1, "45.00", 10)));
        assertEquals(3L, service.anterior("a@teste.com", 4L, 7L).id());
        assertEquals(new BigDecimal("850.00"), service.volumeExercicio("a@teste.com", 7L).volumeTotal());
        assertThrows(ApiException.class, () -> service.anterior("a@teste.com", 3L, 7L));
    }
    @Test void impedeAlteracaoDeSerieAposFinalizacao() {
        Treino t = treino(3, false, "CONCLUIDO", pessoa, exercicio(7));
        when(treinos.findLockedById(3L)).thenReturn(Optional.of(t));
        assertEquals("ESTADO_INVALIDO", assertThrows(ApiException.class,
                () -> service.atualizarSerie("a@teste.com", 3L, 30L, 1L,
                        new ApiDtos.SerieEntrada(1, BigDecimal.TEN, 10))).getCodigo());
    }
    @Test void dashboardSomaVolumePorGrupoComBigDecimal() {
        Exercicio e = exercicio(7);
        Treino t = treino(3, false, "CONCLUIDO", pessoa, e);
        t.setFinalizadoEm(OffsetDateTime.now());
        when(treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(1L)).thenReturn(List.of(t));
        when(series.findForTreinos(List.of(3L))).thenReturn(List.of(
                serie(t.getItens().get(0), 1, 1, "40.25", 10)));
        ApiDtos.DashboardResposta d = service.dashboard("a@teste.com");
        assertEquals(1, d.sessoesConcluidas());
        assertEquals(new BigDecimal("402.50"), d.volumeTotal());
        assertEquals(new BigDecimal("402.50"), d.volumePorGrupo().get("PEITO"));
        assertEquals(BigDecimal.ZERO, d.volumePorGrupo().get("COSTAS"));
    }
    @Test void comparaVolumeEntreSessoesConcluidasDoMesmoPraticante() {
        Exercicio e = exercicio(7);
        Treino a = treino(3, false, "CONCLUIDO", pessoa, e);
        a.setFinalizadoEm(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        Treino b = treino(4, false, "CONCLUIDO", pessoa, e);
        b.setFinalizadoEm(OffsetDateTime.parse("2026-01-02T10:00:00Z"));
        when(treinos.findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(1L)).thenReturn(List.of(b, a));
        when(series.findForTreinos(List.of(4L, 3L))).thenReturn(List.of(
                serie(a.getItens().get(0), 1, 1, "40.50", 10),
                serie(b.getItens().get(0), 2, 1, "45.25", 10)));
        ApiDtos.ComparacaoResposta r = service.comparar("a@teste.com", 3L, 4L, 7L);
        assertEquals(new BigDecimal("47.50"), r.diferenca());
        assertEquals("AUMENTO_CARGA", r.desempenho());
        assertThrows(ApiException.class, () -> service.comparar("a@teste.com", 3L, 8L, 7L));
    }
    @Test void permiteCorrigirSerieDuranteExecucaoSemMudarNumero() {
        Treino t = treino(3, false, "EM_ANDAMENTO", pessoa, exercicio(7));
        SerieExecutada s = serie(t.getItens().get(0), 1, 1, "40.00", 10);
        when(treinos.findLockedById(3L)).thenReturn(Optional.of(t));
        when(series.findById(1L)).thenReturn(Optional.of(s));
        ApiDtos.SerieResposta r = service.atualizarSerie("a@teste.com", 3L, 30L, 1L,
                new ApiDtos.SerieEntrada(1, new BigDecimal("42.50"), 8));
        assertEquals(new BigDecimal("340.00"), r.volume());
        assertEquals("ESTADO_INVALIDO", assertThrows(ApiException.class,
                () -> service.atualizarSerie("a@teste.com", 3L, 30L, 1L,
                        new ApiDtos.SerieEntrada(2, BigDecimal.TEN, 10))).getCodigo());
    }
}
