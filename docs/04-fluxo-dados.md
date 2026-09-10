-> Descrição do Fluxo de Dados

Esta seção descreve como os dados fluem dentro do sistema nas quatro operações fundamentais — inserção, atualização, remoção e busca — utilizando as entidades definidas na Etapa 1 e as estruturas de dados justificadas na Etapa 3.

**Nota de revisão**

Este documento foi atualizado após alinhamento entre os integrantes do grupo. Mudanças relevantes em relação à versão original: SessaoTreino foi unificada à entidade Treino (diferenciada pelo atributo isModelo); SerieRealizada foi renomeada para SerieExecutada; a entidade RecordePessoal foi removida (os campos de recorde passaram a ser sobrescritos diretamente em Exercicio); e o histórico de treinos passou a ser mantido em uma Lista Duplamente Encadeada (ListaDuplaTreinos), no lugar do ArrayList com Comparator definido inicialmente.

- Regra geral: Exclusão Lógica (Soft Delete)

Antes de detalhar os fluxos, estabelece-se uma regra que atravessa todo o sistema: nenhuma entidade histórica sofre exclusão física (hard delete). Isso inclui Exercicio, Treino e seus ItemTreino/SerieExecutada associados.

Essa decisão decorre diretamente do requisito do enunciado: "Manter o histórico mesmo quando um exercício deixar de fazer parte dos treinos atuais." Caso um exercício fosse apagado fisicamente, todos os registros históricos que apontam para ele (ItemTreino, SerieRealizada) ficariam órfãos — referenciando uma entidade inexistente — comprometendo a integridade de consultas e comparações de progressão. Por isso, entidades como Exercicio e Treino possuem um atributo ativo (booleano), que permite "desativar" um registro sem removê-lo, preservando o histórico associado.

- Fluxo de Inserção — Sessão de treino em andamento
O praticante seleciona um Treino com is_modelo = true (a ficha) e aciona iniciarTreinoDeModelo/clonarParaExecucao. Isso cria uma nova instância de Treino, com is_modelo = false, treino_origem_id apontando para o modelo, status = EM_ANDAMENTO e finalizado_em = null.
Nesse mesmo momento, os ItemTreino do modelo são clonados — novas instâncias são criadas, vinculadas ao Treino de execução recém-criado, preservando ordem, series_previstas, reps_previstas e carga_prevista definidos na ficha. Essa clonagem garante que alterações futuras na ficha não afetem essa execução já iniciada.
A cada série executada, é criada uma instância de SerieExecutada (contendo numero_serie, carga_utilizada, repeticoes_realizadas), inserida via add() no final do ArrayList de séries associado ao ItemTreino (de execução) corrente — sempre ao final, sem reordenação, respeitando a ordem cronológica de execução.
A cada série registrada, calcula-se volume_calculado (carga × repetições); o volume do ItemTreino é atualizado de forma incremental; e os campos de recorde em Exercicio (recorde_carga, recorde_reps_para_carga, recorde_volume_sessao) são comparados e sobrescritos caso a nova marca seja superior à registrada.
Ao finalizar o treino, status é alterado para CONCLUIDO e finalizado_em é preenchido. Somente a partir desse momento a execução passa a integrar o histórico "fechado", sendo inserida ao final da ListaDuplaTreinos do praticante (inserirFim), disponível para cálculos de comparação e progressão.

- Fluxo de Busca — Consulta de histórico por exercício, ordenado por critério

Exemplo de cenário: o praticante deseja consultar o histórico de séries do exercício "Supino Reto", ordenado por carga, da maior para a menor.

Percorre-se a ListaDuplaTreinos do praticante (busca sequencial, O(n)), e, para cada Treino de execução, verifica-se se algum ItemTreino associado corresponde ao exercicio_id pesquisado (método obterItemPorExercicio). Essa etapa é necessária porque SerieExecutada não possui referência direta ao exercício — apenas ao ItemTreino ao qual pertence.
Para cada ItemTreino correspondente, agregam-se as SerieExecutada associadas em uma lista única de resultado.
Sobre essa lista já filtrada, aplica-se ordenação por carga, em ordem decrescente (método ordenarPorCarga de ListaDuplaTreinos). O mesmo mecanismo se aplica caso o critério escolhido seja data, repetições ou volume, bastando invocar o método de ordenação correspondente (ordenarPorData, ordenarPorRepeticoes, ordenarPorVolume).
Ressalva de desempenho: a etapa de filtragem (passo 1) seria resolvida em complexidade O(1) caso implementada com uma tabela hash (HashMap<Exercicio, List<ItemTreino>>). O grupo optou por resolver essa etapa com busca sequencial por esse conteúdo ainda não ter sido trabalhado em profundidade na disciplina até o momento da elaboração deste projeto, estando ciente do custo de desempenho em bases de dados de grande volume.
Comparação com o treino anterior: por se tratar de uma Lista Duplamente Encadeada, a obtenção do treino imediatamente anterior a uma execução (obterTreinoAnteriorPorExercicio) é feita em O(1) através do ponteiro anterior do nó correspondente, sem necessidade de percorrer a lista desde o início.

- Fluxo de Atualização
Atualização do modelo de treino (ficha): alterações em um Treino com is_modelo = true — como reordenar a sequência de ItemTreino ou alterar series_previstas/reps_previstas — afetam exclusivamente o modelo/ficha, sendo aplicadas via acesso direto por índice no ArrayList de ItemTreino daquele modelo. Como cada execução (is_modelo = false) possui sua própria cópia clonada dos ItemTreino (ver seção 4.2, passo 2), nenhum registro de execução já existente é alterado por essa operação.
Atualização de dados de execução: uma SerieExecutada só pode ser corrigida enquanto o Treino de execução ao qual pertence estiver com status = EM_ANDAMENTO (ex.: correção de um valor de carga digitado incorretamente durante o próprio treino). Uma vez que a execução é concluída (status = CONCLUIDO), o registro é tratado como histórico imutável.
Justificativa: essa separação entre "modelo editável" e "histórico imutável" evita a chamada reescrita da história — se a ficha "Treino A" for reordenada hoje, os registros de execuções já realizadas no passado devem continuar refletindo exatamente a ordem, carga e repetições praticadas naquele dia, preservando o sentido real das análises de progressão e desempenho.

- Fluxo de Remoção (Exclusão Lógica)
Nenhuma entidade histórica é removida fisicamente do sistema.
Quando o praticante decide não usar mais um exercício, o sistema marca ativo = false no registro correspondente de Exercicio, mantendo-o intacto.
Como consequência, todo o histórico associado (Treino, ItemTreino, SerieExecutada) permanece íntegro e consultável, pois continua referenciando um Exercicio que ainda existe no sistema, apenas marcado como inativo.
Telas de criação/edição de fichas passam a exibir somente exercícios com ativo = true; telas de histórico e evolução continuam funcionando normalmente, independentemente do status do exercício.

- Critérios de Priorização e Ordenação — Síntese
Situação	Critério aplicado	Mecanismo
Séries dentro de um treino	Ordem de execução (cronológica, fixa)	Inserção sequencial no ArrayList (add()), sem reordenação
Recordes pessoais	Apenas o valor mais recente (sem histórico)	Atributo sobrescrito em Exercicio — limitação assumida
Histórico de treinos (execuções)	Data, carga, repetições ou volume; acesso rápido ao "anterior"	Lista Duplamente Encadeada (ListaDuplaTreinos/NoTreino)
Consulta de treinos por exercício	Filtro por chave (exercício), depois ordenação	Busca sequencial na lista encadeada, seguida de ordenação sob demanda
Ordem de exercícios na ficha	Definida e reordenável pelo praticante	Acesso indexado no ArrayList de ItemTreino do modelo