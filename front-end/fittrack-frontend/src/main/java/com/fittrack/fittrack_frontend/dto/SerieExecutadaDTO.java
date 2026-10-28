package com.fittrack.fittrack_frontend.dto;

/**
 * Série efetivamente realizada pelo praticante e enviada para persistência do histórico.
 * O volume pode ser calculado a partir da carga e das repetições; o vínculo com o item permite
 * enviar as séries em uma lista plana sem perder a associação com o exercício da sessão.
 *
 * @param id identificador da série persistida, nulo antes da gravação
 * @param numeroSerie posição da série no item de treino
 * @param cargaUtilizada carga efetivamente utilizada pelo praticante
 * @param repeticoesRealizadas repetições concluídas
 * @param horarioConclusao horário local em que a série foi confirmada
 * @param volumeCalculado volume da série, normalmente carga multiplicada pelas repetições
 * @param concluida indica se a série foi efetivamente completada
 * @param itemTreinoId identificador do item de treino ao qual a série pertence
 */
public record SerieExecutadaDTO(
    /** Identificador gerado pelo back-end após a persistência. */
    Long id,
    /** Sequência da série no exercício. */
    Integer numeroSerie,
    /** Peso realmente registrado, diferente da carga prevista no item. */
    Double cargaUtilizada,
    /** Quantidade efetiva de repetições concluídas. */
    Integer repeticoesRealizadas,
    /** Horário utilizado para registrar a conclusão da série. */
    String horarioConclusao,
    /** Valor do volume calculado para exibição e consolidação. */
    Double volumeCalculado,
    /** Estado de conclusão da execução. */
    boolean concluida,
    /** Chave do ItemTreino que contextualiza esta série na sessão. */
    Long itemTreinoId
) {}
