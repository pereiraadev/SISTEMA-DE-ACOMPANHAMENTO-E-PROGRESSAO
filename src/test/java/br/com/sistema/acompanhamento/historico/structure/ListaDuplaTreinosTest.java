package br.com.sistema.acompanhamento.historico.structure;

import br.com.sistema.acompanhamento.historico.domain.TreinoComExercicios;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class ListaDuplaTreinosTest {

    // Representa um treino simples usado apenas nos testes
    static class TreinoTeste implements TreinoComExercicios {

        private String nome;
        private ArrayList<Long> exercicios = new ArrayList<>();
        private boolean modelo;

        public TreinoTeste(String nome, Long... idsExercicios) {
            this.nome = nome;

            for (Long id : idsExercicios) {
                exercicios.add(id);
            }
        }

        // Verifica se o exercício está presente no treino
        @Override
        public boolean possuiExercicio(Long exercicioId) {
            return exercicios.contains(exercicioId);
        }

        public String getNome() {
            return nome;
        }

        // Define se o treino é um modelo
        public void setModelo(boolean modelo) {
            this.modelo = modelo;
        }

        // Informa se o treino é um modelo
        public boolean isModelo() {
            return modelo;
        }
    }

    @Test
    void deveInserirTreinosNaOrdemCorreta() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        TreinoTeste treinoA = new TreinoTeste("Treino A", 1L);
        TreinoTeste treinoB = new TreinoTeste("Treino B", 2L);
        TreinoTeste treinoC = new TreinoTeste("Treino C", 1L);

        // Adiciona os treinos na lista
        lista.inserirFim(treinoA);
        lista.inserirFim(treinoB);
        lista.inserirFim(treinoC);

        // Verifica o tamanho e os ponteiros
        assertEquals(3, lista.getTamanho());
        assertSame(treinoA, lista.getInicio().getTreino());
        assertSame(treinoC, lista.getFim().getTreino());

        assertSame(treinoB, lista.getInicio().getProximo().getTreino());
        assertSame(treinoB, lista.getFim().getAnterior().getTreino());
    }

    @Test
    void deveEncontrarTreinoAnteriorDoMesmoExercicio() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        // Segunda e sexta possuem o exercício 1
        TreinoTeste segunda = new TreinoTeste("Segunda", 1L);
        TreinoTeste quarta = new TreinoTeste("Quarta", 2L);
        TreinoTeste sexta = new TreinoTeste("Sexta", 1L);

        lista.inserirFim(segunda);
        lista.inserirFim(quarta);
        lista.inserirFim(sexta);

        // Procura o treino anterior de sexta com o exercício 1
        TreinoTeste anterior = lista.obterTreinoAnteriorPorExercicio(sexta, 1L);

        // Deve ignorar quarta e encontrar segunda
        assertSame(segunda, anterior);
    }

    @Test
    void deveRetornarNullQuandoNaoExisteTreinoAnterior() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        TreinoTeste segunda = new TreinoTeste("Segunda", 1L);
        TreinoTeste quarta = new TreinoTeste("Quarta", 2L);

        lista.inserirFim(segunda);
        lista.inserirFim(quarta);

        // Não existe treino anterior a segunda
        TreinoTeste anterior = lista.obterTreinoAnteriorPorExercicio(segunda, 1L);

        assertNull(anterior);
    }

    @Test
    void deveRetornarNullQuandoTreinoAtualNaoEstaNaLista() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        TreinoTeste segunda = new TreinoTeste("Segunda", 1L);
        TreinoTeste quarta = new TreinoTeste("Quarta", 1L);
        TreinoTeste sabado = new TreinoTeste("Sábado", 1L);

        lista.inserirFim(segunda);
        lista.inserirFim(quarta);

        // Sábado não foi adicionado à lista
        TreinoTeste anterior = lista.obterTreinoAnteriorPorExercicio(sabado, 1L);

        // Não deve encontrar nenhum treino anterior
        assertNull(anterior);
    }

    @Test
    void deveEncontrarSessaoAnteriorMaisRecente() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        TreinoTeste segunda = new TreinoTeste("Segunda", 1L);
        TreinoTeste quarta = new TreinoTeste("Quarta", 1L);
        TreinoTeste sexta = new TreinoTeste("Sexta", 2L);
        TreinoTeste domingo = new TreinoTeste("Domingo", 1L);

        // Adiciona os treinos na ordem em que aconteceram
        lista.inserirFim(segunda);
        lista.inserirFim(quarta);
        lista.inserirFim(sexta);
        lista.inserirFim(domingo);

        // Procura a sessão anterior de supino
        TreinoTeste anterior = lista.obterTreinoAnteriorPorExercicio(domingo, 1L);

        // Deve encontrar quarta, que é a sessão mais recente
        assertSame(quarta, anterior);
    }

    @Test
    void deveAdicionarSomenteTreinosRealizados() {

        ListaDuplaTreinos<TreinoTeste> lista = new ListaDuplaTreinos<>();

        TreinoTeste segunda = new TreinoTeste("Segunda", 1L);
        TreinoTeste fichaModelo = new TreinoTeste("Ficha ABC", 2L);
        TreinoTeste quarta = new TreinoTeste("Quarta", 1L);

        // Marca a ficha como modelo
        fichaModelo.setModelo(true);

        // Adiciona somente os treinos realizados
        for (TreinoTeste treino : new TreinoTeste[] {
                segunda, fichaModelo, quarta }) {

            if (!treino.isModelo()) {
                lista.inserirFim(treino);
            }
        }

        // Confere se apenas dois treinos foram adicionados
        assertEquals(2, lista.getTamanho());

        // Confere a ordem dos treinos
        assertSame(segunda, lista.getInicio().getTreino());
        assertSame(quarta, lista.getFim().getTreino());
    }
}