package br.com.sistema.acompanhamento.comparativo;

import br.com.sistema.acompanhamento.comparativo.dto.ResumoSessao;
import br.com.sistema.acompanhamento.comparativo.dto.VariacaoResposta;
import br.com.sistema.acompanhamento.treino.ItemTreino;
import br.com.sistema.acompanhamento.treino.Treino;

// so as contas do comparativo, sem banco nem spring
public final class ComparativoCalculador {

    private ComparativoCalculador() { }

    public static ResumoSessao resumir(Treino treino, ItemTreino item) {
        return new ResumoSessao(
                treino.getId(),
                treino.getDataTreino(),
                item.obterMaiorCarga(),
                item.contarRepeticoes(),
                item.getSeries().size(),
                item.calcularVolumeTotal());
    }

    public static VariacaoResposta comparar(ResumoSessao atual, ResumoSessao anterior) {
        Variacao carga = Variacao.de(atual.maiorCarga(), anterior.maiorCarga());
        Variacao repeticoes = Variacao.de(atual.totalRepeticoes(), anterior.totalRepeticoes());
        Variacao volume = Variacao.de(atual.volumeTotal(), anterior.volumeTotal());

        // mesma carga mas mais repeticoes tambem conta como evolucao
        boolean evoluiuPorRepeticoes = carga == Variacao.MANUTENCAO && repeticoes == Variacao.AUMENTO;

        return new VariacaoResposta(
                carga,
                repeticoes,
                volume,
                atual.maiorCarga().subtract(anterior.maiorCarga()),
                atual.totalRepeticoes() - anterior.totalRepeticoes(),
                atual.volumeTotal().subtract(anterior.volumeTotal()),
                evoluiuPorRepeticoes);
    }
}
