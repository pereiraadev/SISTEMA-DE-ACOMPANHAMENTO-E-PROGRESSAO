-> Descrição do Fluxo de Dados

Esta seção descreve como os dados fluem dentro do sistema nas quatro operações fundamentais — inserção, atualização, remoção e busca — utilizando as entidades definidas na Etapa 1 e as estruturas de dados justificadas na Etapa 3.

**Regra geral: Exclusão Lógica (Soft Delete)**

Antes de detalhar os fluxos, estabelece-se uma regra que atravessa todo o sistema: **nenhuma entidade histórica sofre exclusão física (hard delete)**. Isso inclui `Exercicio`, `Treino`, `SessaoTreino`, `ItemTreino`, `SerieRealizada` e `RecordePessoal`.

Essa decisão decorre diretamente do requisito do enunciado: *"Manter o histórico mesmo quando um exercício deixar de fazer parte dos treinos atuais."* Caso um exercício fosse apagado fisicamente, todos os registros históricos que apontam para ele (`ItemTreino`, `SerieRealizada`) ficariam órfãos — referenciando uma entidade inexistente — comprometendo a integridade de consultas e comparações de progressão. Por isso, entidades como `Exercicio` e `Treino` possuem um atributo `ativo` (booleano), que permite "desativar" um registro sem removê-lo, preservando o histórico associado.

**Fluxo de Inserção — Sessão de treino em andamento**

1. Ao iniciar o treino, é criada uma instância de `SessaoTreino`, com `status = EM_ANDAMENTO` e `data_hora_fim = null` (preenchido apenas na finalização da sessão).
2. Ao escolher um exercício para realizar, é criada uma instância de `ItemTreino`, vinculando `sessao_treino_id` e `exercicio_id`, com `ordem_execucao` definida pela sequência em que os exercícios são efetivamente realizados naquele treino.
3. A cada série executada, é criada uma instância de `SerieRealizada` (contendo `numero_serie`, `carga`, `repeticoes`), inserida via `add()` no final do ArrayList de séries associado ao `ItemTreino` corrente — sempre ao final, sem reordenação, respeitando a ordem cronológica de execução.
4. A cada série registrada, calcula-se `volume_serie` (carga × repetições), e o `volume_total_exercicio` do `ItemTreino` correspondente é atualizado de forma incremental.
5. Ao finalizar o treino, `status` é alterado para `CONCLUIDO` e `data_hora_fim` é preenchido. Somente a partir desse momento a sessão passa a integrar o histórico "fechado", disponível para cálculos de comparação e progressão.

**Fluxo de Busca — Consulta de histórico por exercício, ordenado por critério**

Exemplo de cenário: o praticante deseja consultar o histórico de séries do exercício "Supino Reto", ordenado por carga, da maior para a menor.

1. Percorre-se o ArrayList de `ItemTreino` (busca sequencial, O(n)), filtrando apenas os registros cujo `exercicio_id` corresponda ao exercício pesquisado. Essa etapa é necessária porque `SerieRealizada` não possui referência direta ao exercício — apenas ao `ItemTreino` ao qual pertence.
2. Para cada `ItemTreino` filtrado, acessa-se o ArrayList de `SerieRealizada` associado, agregando todos os registros encontrados em uma lista única de resultado.
3. Sobre essa lista já filtrada, aplica-se `Collections.sort()` com um `Comparator` por `carga`, em ordem decrescente — atendendo ao critério de ordenação solicitado pelo usuário. O mesmo mecanismo se aplica caso o critério escolhido seja data, repetições ou volume, bastando trocar a implementação do `Comparator`.
4. **Ressalva de desempenho:** a etapa de filtragem (passo 1) seria resolvida em complexidade O(1) caso implementada com uma tabela hash (`HashMap<Exercicio, List<ItemTreino>>`). O grupo optou por resolver essa etapa com busca sequencial por esse conteúdo ainda não ter sido trabalhado em profundidade na disciplina até o momento da elaboração deste projeto, estando ciente do custo de desempenho em bases de dados de grande volume.

**Fluxo de Atualização**

1. **Atualização do modelo de treino (ficha):** alterações em `Treino` e `ItemTreinoModelo` — como reordenar a sequência de exercícios ou alterar `series_planejadas`/`repeticoes_alvo` — afetam exclusivamente o modelo/ficha, sendo aplicadas via acesso direto por índice no ArrayList de `ItemTreinoModelo` (por exemplo, trocando a posição de dois elementos). Nenhum registro de `SessaoTreino`, `ItemTreino` ou `SerieRealizada` já existente é alterado por essa operação.
2. **Atualização de dados de execução:** uma `SerieRealizada` só pode ser corrigida enquanto sua `SessaoTreino` estiver com `status = EM_ANDAMENTO` (ex.: correção de um valor de carga digitado incorretamente durante o próprio treino). Uma vez que a sessão é concluída (`status = CONCLUIDO`), o registro é tratado como histórico imutável.
3. **Justificativa:** essa separação entre "modelo editável" e "histórico imutável" evita a chamada *reescrita da história* — se a ficha "Treino A" for reordenada hoje, os registros de sessões já realizadas no passado devem continuar refletindo exatamente a ordem, carga e repetições praticadas naquele dia, preservando o sentido real das análises de progressão e desempenho.

**Fluxo de Remoção (Exclusão Lógica)**

1. Nenhuma entidade histórica é removida fisicamente do sistema.
2. Quando o praticante decide não usar mais um exercício, o sistema marca `ativo = false` no registro correspondente de `Exercicio` (ou remove apenas a entrada em `ItemTreinoModelo` daquela ficha específica, mantendo o `Exercicio` intacto caso ele ainda seja usado em outras fichas).
3. Como consequência, todo o histórico associado (`ItemTreino`, `SerieRealizada`, `RecordePessoal`) permanece íntegro e consultável, pois continua referenciando um `Exercicio` que ainda existe no sistema, apenas marcado como inativo.
4. Telas de criação/edição de fichas passam a exibir somente exercícios com `ativo = true`; telas de histórico e evolução continuam funcionando normalmente, independentemente do status do exercício.

**Critérios de Priorização e Ordenação — Síntese**

| Situação | Critério aplicado | Mecanismo |
|----------|-------------------|-----------|
| Séries dentro de um treino | Ordem de execução (cronológica, fixa) | Inserção sequencial no ArrayList (`add()`), sem reordenação |
| Progressão de recordes pessoais | Sempre o mais recente/maior no topo | Pilha (`push`/`peek`) |
| Histórico de sessões de treino | Data, carga, repetições ou volume (à escolha do usuário) | ArrayList + `Comparator`, ordenado sob demanda |
| Consulta de treinos por exercício | Filtro por chave (exercício), depois ordenação | Busca sequencial (ArrayList) seguida de `Comparator` |
| Ordem de exercícios na ficha | Definida e reordenável pelo praticante | Acesso indexado no ArrayList de `ItemTreinoModelo` |