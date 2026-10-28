package br.com.sistema.acompanhamento.historico.estrutura;

// Representa um nó da lista duplamente encadeada
public class NoTreino<T> {

    // Guarda o treino e as ligações com os outros nós
    private T treino;
    private NoTreino<T> anterior;
    private NoTreino<T> proximo;

    // Cria um nó com o treino recebido
    public NoTreino(T treino) {
        this.treino = treino;
    }

    // Retorna o treino guardado no nó
    public T getTreino() {
        return treino;
    }

    // Retorna o nó anterior
    public NoTreino<T> getAnterior() {
        return anterior;
    }

    // Define qual é o nó anterior
    public void setAnterior(NoTreino<T> anterior) {
        this.anterior = anterior;
    }

    // Retorna o próximo nó
    public NoTreino<T> getProximo() {
        return proximo;
    }

    // Define qual é o próximo nó
    public void setProximo(NoTreino<T> proximo) {
        this.proximo = proximo;
    }
}