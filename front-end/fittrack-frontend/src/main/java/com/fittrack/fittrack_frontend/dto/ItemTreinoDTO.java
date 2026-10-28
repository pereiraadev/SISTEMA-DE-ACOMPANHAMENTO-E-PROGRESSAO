package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Exercício posicionado dentro de uma ficha ou sessão de treino, com prescrição e execução.
 *
 * @param id identificador do item relacionado ao treino
 * @param ordem posição do exercício na sequência planejada
 * @param seriesPrevistas quantidade de séries prescritas
 * @param repsPrevistas repetições de referência por série
 * @param cargaPrevista carga sugerida para orientar o praticante
 * @param concluido indica se o item foi marcado como concluído no back-end
 * @param exercicio dados do exercício associado
 * @param seriesRealizadas execuções já registradas para este item
 */
public record ItemTreinoDTO(
    /** Chave do item de treino e vínculo usado para associar séries executadas. */
    Long id,
    /** Ordem de exibição do exercício na sessão. */
    Integer ordem,
    /** Número de séries planejadas. */
    Integer seriesPrevistas,
    /** Alvo de repetições por série. */
    Integer repsPrevistas,
    /** Referência de carga planejada, distinta da carga efetivamente levantada. */
    Double cargaPrevista,
    /** Estado de conclusão informado pelo domínio. */
    Boolean concluido,
    /** Dados de apresentação do exercício associado. */
    ExercicioDTO exercicio,
    /** Séries previamente salvas para retomar uma sessão em andamento. */
    List<SerieExecutadaDTO> seriesRealizadas
) {}
