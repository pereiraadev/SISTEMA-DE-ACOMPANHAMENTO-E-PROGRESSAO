package br.com.sistema.acompanhamento.historico.estrutura;

import br.com.sistema.acompanhamento.treino.Treino;

public class NoTreino {

    private final Treino treino;
    private NoTreino anterior;
    private NoTreino proximo;

    public NoTreino(Treino treino) {
        if (treino == null) {
            throw new IllegalArgumentException("Treino não pode ser nulo.");
        }
        this.treino = treino;
    }

    public Treino getTreino() { return treino; }
    public NoTreino getAnterior() { return anterior; }
    public NoTreino getProximo() { return proximo; }

    // package-private, so a lista mexe nos ponteiros
    void setAnterior(NoTreino anterior) { this.anterior = anterior; }
    void setProximo(NoTreino proximo) { this.proximo = proximo; }
}
