# Integração do histórico

`TreinoWorkflowService` consulta as entidades oficiais `Treino`, `ItemTreino`, `SerieExecutada` e `Exercicio` dentro de transações de leitura. Monta adaptadores pequenos de `TreinoComSeries` e `SerieHistorico` e chama `CarregadorHistorico`, que utiliza `ListaDuplaTreinos` e `NoTreino`.

Somente execuções do praticante autenticado com `status=CONCLUIDO` e `finalizadoEm` preenchido entram na lista. O carregador ignora modelos, ordena pela hora real convertida a UTC e desempata pelo ID. IDs persistidos repetidos não entram duas vezes. A lista é reconstruída a cada consulta, sem manter entidades JPA em memória entre requisições.

As séries são consultadas em lote pelos IDs dos treinos. Carga e volume nas respostas e nos cálculos integrados usam `BigDecimal`. Os métodos acadêmicos anteriores que retornam `double` permanecem disponíveis para compatibilidade. A coluna `volume_calculado` é gerada pelo PostgreSQL; as respostas imediatas usam a mesma fórmula `carga_utilizada × repeticoes_realizadas`.

O enum de grupo muscular corresponde aos cinco valores descritos em `docs/01-entidades.md`. O campo JPA de `Exercicio` é `String`; um valor desconhecido no banco não é atribuído a um grupo do enum. Antes da homologação, conferir esses valores com o banco de teste.
