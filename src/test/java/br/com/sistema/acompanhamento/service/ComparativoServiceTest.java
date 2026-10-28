package br.com.sistema.acompanhamento.service;

import static br.com.sistema.acompanhamento.Montador.emAndamento;
import static br.com.sistema.acompanhamento.Montador.exercicio;
import static br.com.sistema.acompanhamento.Montador.item;
import static br.com.sistema.acompanhamento.Montador.modelo;
import static br.com.sistema.acompanhamento.Montador.serie;
import static br.com.sistema.acompanhamento.Montador.treino;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.sistema.acompanhamento.Exercicio;
import br.com.sistema.acompanhamento.Praticante;
import br.com.sistema.acompanhamento.Treino;
import br.com.sistema.acompanhamento.dto.ComparativoDTO;
import br.com.sistema.acompanhamento.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.repository.TreinoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComparativoServiceTest {

    private static final Exercicio SUPINO = exercicio(1L, "Supino", "PEITO");
    private static final Exercicio AGACHAMENTO = exercicio(2L, "Agachamento", "PERNAS");

    private TreinoRepository treinoRepository;
    private ComparativoService service;

    // Treinos concluidos: 10 (supino 80x8 3 series), 11 (agachamento), 12 (supino 82,5x8 3 series).
    private Treino treino10;
    private Treino treino11;
    private Treino treino12;

    @BeforeEach
    void preparar() {
        PraticanteRepository praticanteRepository = mock(PraticanteRepository.class);
        treinoRepository = mock(TreinoRepository.class);
        service = new ComparativoService(praticanteRepository, treinoRepository);

        Praticante ana = mock(Praticante.class);
        when(ana.getId()).thenReturn(7L);
        when(praticanteRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(ana));

        treino10 = treino(10L, LocalDate.of(2026, 10, 1),
                item(SUPINO, serie(1, "80.00", 8), serie(2, "80.00", 8), serie(3, "80.00", 8)));
        treino11 = treino(11L, LocalDate.of(2026, 10, 8),
                item(AGACHAMENTO, serie(1, "100.00", 5)));
        treino12 = treino(12L, LocalDate.of(2026, 10, 15),
                item(SUPINO, serie(1, "82.50", 8), serie(2, "82.50", 8), serie(3, "82.50", 8)));

        when(treinoRepository.buscarExecucoesConcluidas(7L)).thenReturn(List.of(treino10, treino11, treino12));
        when(treinoRepository.findByIdAndPraticanteId(10L, 7L)).thenReturn(Optional.of(treino10));
        when(treinoRepository.findByIdAndPraticanteId(11L, 7L)).thenReturn(Optional.of(treino11));
        when(treinoRepository.findByIdAndPraticanteId(12L, 7L)).thenReturn(Optional.of(treino12));
    }

    @Test
    void comparaComOTreinoAnteriorQueFezOExercicio() {
        ComparativoDTO resposta = service.comparar("ana@email.com", 12L, 1L);

        // O treino 11 so tem agachamento, entao o anterior do supino e o treino 10.
        assertThat(resposta.resultado()).isEqualTo("AUMENTO_CARGA");
        assertThat(resposta.anterior().treinoId()).isEqualTo(10L);
        assertThat(resposta.atual().treinoId()).isEqualTo(12L);
        assertThat(resposta.diferencaVolume()).isEqualByComparingTo("60.00");
    }

    @Test
    void primeiraExecucaoNaoTemAnterior() {
        ComparativoDTO resposta = service.comparar("ana@email.com", 10L, 1L);

        assertThat(resposta.resultado()).isEqualTo("SEM_DADOS");
        assertThat(resposta.anterior()).isNull();
        assertThat(resposta.diferencaVolume()).isNull();
        assertThat(resposta.atual()).isNotNull();
    }

    @Test
    void treinoEmAndamentoComparaComOUltimoConcluido() {
        Treino emAndamento = emAndamento(14L, LocalDate.of(2026, 10, 22), item(SUPINO, serie(1, "85.00", 5)));
        when(treinoRepository.findByIdAndPraticanteId(14L, 7L)).thenReturn(Optional.of(emAndamento));

        ComparativoDTO resposta = service.comparar("ana@email.com", 14L, 1L);

        assertThat(resposta.anterior().treinoId()).isEqualTo(12L);
        assertThat(resposta.resultado()).isEqualTo("AUMENTO_CARGA");
    }

    @Test
    void mesmaCargaComMaisRepeticoes() {
        Treino emAndamento = emAndamento(14L, LocalDate.of(2026, 10, 22), item(SUPINO, serie(1, "82.50", 10)));
        when(treinoRepository.findByIdAndPraticanteId(14L, 7L)).thenReturn(Optional.of(emAndamento));

        assertThat(service.comparar("ana@email.com", 14L, 1L).resultado()).isEqualTo("AUMENTO_REPETICOES");
    }

    @Test
    void treinoDeOutroUsuarioNaoEncontrado() {
        when(treinoRepository.findByIdAndPraticanteId(50L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.comparar("ana@email.com", 50L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void treinoModeloNaoTemExecucao() {
        Treino ficha = modelo(60L, item(SUPINO));
        when(treinoRepository.findByIdAndPraticanteId(60L, 7L)).thenReturn(Optional.of(ficha));

        assertThatThrownBy(() -> service.comparar("ana@email.com", 60L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exercicioQueNaoEstaNoTreino() {
        assertThatThrownBy(() -> service.comparar("ana@email.com", 12L, 2L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void praticanteInexistente() {
        assertThatThrownBy(() -> service.comparar("fantasma@email.com", 12L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
