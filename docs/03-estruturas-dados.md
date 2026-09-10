-> Escolha da Estrutura de Dados

Para cada coleção de dados relevante do sistema, foi realizada uma análise baseada em três critérios: (i) a operação mais frequente sobre a coleção, (ii) se a ordem de inserção possui relevância para alguma regra de negócio, e (iii) se há necessidade de priorização/ordenação por critérios distintos da ordem de chegada. A estrutura de dados foi escolhida com base no comportamento resultante dessa análise, e não por preferência arbitrária do grupo.

- Séries dentro de um ItemTreino

Estrutura escolhida: ArrayList (Lista - array dinâmico)

Durante a execução de um treino, as séries de um exercício são inseridas sempre ao final, seguindo rigorosamente a ordem de execução (1ª, 2ª, 3ª série, e assim por diante) — o praticante nunca insere uma série no meio da sequência já registrada, nem reordena séries já concluídas. Não há necessidade de acesso aleatório a uma série específica fora da ordem sequencial.

O ArrayList foi escolhido em vez da Lista Encadeada (LinkedList) porque, nesse cenário, não existe ganho algum em usar uma estrutura mais flexível (que permite inserção/remoção em qualquer posição) para um comportamento que só precisa de inserção no fim. Além disso, o ArrayList oferece obtenção do tamanho da coleção em tempo O(1), o que é utilizado diretamente para atender ao requisito de "visualizar quantas séries já foram concluídas" durante o treino, sem necessidade de percorrer e contar elemento por elemento.

- Recordes Pessoais — decisão de simplificação (sem entidade dedicada)

Estrutura escolhida: nenhuma coleção própria — atributos sobrescritos em Exercicio

Inicialmente, o grupo havia definido uma entidade RecordePessoal, associada a uma estrutura de Pilha, para manter o histórico de progressão de recordes ao longo do tempo (ex.: 70kg → 75kg → 80kg → 85kg no supino, cada marco com sua respectiva data).

Após revisão, o grupo optou por simplificar essa parte do sistema, substituindo a entidade dedicada por três atributos sobrescritos diretamente em Exercicio: recordeCarga, recordeRepsParaCarga e recordeVolumeSessao. Sempre que uma nova série é registrada, esses valores são comparados e, se superados, atualizados — sem preservar o valor anterior nem a data em que cada marco foi alcançado.

Limitação assumida conscientemente: essa simplificação implica que o sistema deixa de responder com precisão a exigências do enunciado como "identificar exercícios nos quais o praticante apresentou aumento de carga ao longo do tempo", pois não há mais registro de quando cada recorde foi batido, nem de quanto tempo se passou entre uma evolução e outra — apenas o valor mais recente é conhecido. O grupo decidiu aceitar essa limitação em favor da simplicidade de implementação, priorizando o tempo disponível para as demais entregas do projeto. A recuperação de parte dessa informação ainda é indiretamente possível ao se percorrer o histórico completo de treinos (via ListaDuplaTreinos, seção 3.3) e comparar manualmente os valores de carga entre execuções, embora sem o mesmo desempenho e clareza que uma estrutura dedicada ofereceria.

- Sessões de Treino (histórico completo do praticante)

Estrutura escolhida: Lista Duplamente Encadeada (implementação própria — ListaDuplaTreinos / NoTreino)

O histórico de sessões de treino precisa atender a dois tipos de operação com alta frequência: (i) inserir uma nova sessão sempre ao final, conforme os treinos são concluídos, e (ii) acessar rapidamente o treino anterior a partir do treino atual — requisito explícito do enunciado: "exibir, ao iniciar um novo treino, informações do treino anterior para servir como referência" e "comparar o desempenho atual com o desempenho do treino anterior".

Optou-se por uma Lista Duplamente Encadeada, implementada com uma estrutura de nós (NoTreino), cada um mantendo referência tanto ao nó anterior quanto ao próximo. A justificativa central está na direção de navegação exigida: em uma lista simplesmente encadeada, obter o nó anterior a um nó qualquer exige percorrer a lista desde o início até encontrá-lo, em tempo O(n) a cada consulta. Na lista duplamente encadeada, estando posicionado em um nó (o treino atual), o acesso ao anterior é feito diretamente pelo ponteiro anterior, em tempo O(1) — sem necessidade de percorrer a coleção. Esse ganho justifica o custo adicional de memória de manter dois ponteiros por nó, dado que a operação "obter treino anterior" é executada com alta frequência (potencialmente a cada novo treino iniciado).

A ordenação por critérios variáveis (data, carga, repetições ou volume), quando solicitada pelo usuário, é realizada percorrendo a lista encadeada e produzindo uma coleção auxiliar ordenada (métodos privados ordenarPorData, ordenarPorCarga, ordenarPorRepeticoes, ordenarPorVolume de ListaDuplaTreinos), aplicada apenas sob demanda.

- Consulta de treinos por exercício realizado

Estrutura escolhida: ArrayList com busca sequencial (limitação documentada)

O requisito "permitir consultar todos os treinos em que determinado exercício foi realizado" representa um problema de busca por chave, para o qual a estrutura mais adequada em termos de eficiência seria uma tabela hash (HashMap), mapeando cada Exercício a uma lista dos registros em que ele aparece, com complexidade de acesso O(1).

Como esse conteúdo ainda não foi trabalhado em profundidade na disciplina até o momento da elaboração deste projeto, o grupo optou conscientemente por resolver esse requisito por meio de busca sequencial sobre a estrutura ArrayList já utilizada para o histórico de sessões, com complexidade O(n) por consulta. O grupo está ciente de que essa abordagem apresenta desempenho inferior ao de uma tabela hash em bases de dados de grande volume, e reconhece essa limitação como uma oportunidade futura de otimização.

- Itens do Treino Modelo (ordem de execução da ficha)

Estrutura escolhida: ArrayList

Uma ficha de treino (ex.: "Treino A") contém tipicamente um número pequeno de exercícios (entre 3 e 8), organizados em uma ordem de execução definida pelo praticante. Diferentemente da coleção de séries, essa ordem é frequentemente alterada pelo próprio praticante (por exemplo, por orientação de um profissional, ao inverter a prioridade de execução dos exercícios).

Como a reordenação exige o acesso direto a posições específicas da coleção (ex.: trocar o item da posição 1 com o item da posição 3), o ArrayList foi escolhido por oferecer acesso indexado em O(1). Uma lista encadeada, por não permitir acesso direto por índice, exigiria percorrer a estrutura a partir do início a cada operação de reordenação, o que é desnecessário dado o tamanho reduzido dessa coleção.

- Síntese das escolhas
Coleção	Estrutura	Justificativa central
Séries dentro de ItemTreino	ArrayList	Inserção só no fim, nunca reordena, size() em O(1)
Recordes Pessoais	(sem estrutura própria)	Simplificado para atributos sobrescritos em Exercicio; histórico de progressão não é mantido (limitação assumida)
Sessões de Treino (histórico)	Lista Duplamente Encadeada	Acesso O(1) ao treino anterior a partir do atual, sem percorrer a lista
Consulta de treinos por exercício	ArrayList (com ressalva)	Ideal seria HashMap O(1); não implementado por não ter sido estudado ainda
Itens do Treino Modelo (ordem da ficha)	ArrayList	Poucos itens, reordenação frequente por posição

A predominância de ArrayList nas demais coleções reflete o comportamento real dos dados: coleções pequenas, com inserção no fim e eventual necessidade de reordenação por índice. A exceção é o histórico de Sessões de Treino, cujo padrão de acesso bidirecional (avançar e retroceder a partir de um ponto qualquer) motivou o uso de uma Lista Duplamente Encadeada, e os Recordes Pessoais, cuja simplificação eliminou a necessidade de estrutura de dados própria, ao custo de não preservar a linha do tempo das evoluções de carga.

- Sobre a clonagem de treinos (Treino modelo vs. execução)

A entidade Treino unifica modelo (ficha reutilizável) e execução (sessão realizada em uma data), diferenciados pelo atributo isModelo e pelo auto-relacionamento treinoOrigem. Ao iniciar um treino a partir de um modelo (iniciarTreinoDeModelo/clonarParaExecucao), é criada uma nova instância de Treino, com isModelo = false, referenciando o modelo de origem — e, criticamente, os ItemTreino associados também são clonados (novas instâncias, não reaproveitadas), preservando os valores planejados no momento da execução. Isso garante que alterações futuras na ficha modelo (ex.: reordenar exercícios, mudar séries previstas) não afetem execuções já registradas, mantendo o histórico imutável — mesmo princípio que motivou a separação inicial entre "ficha" e "execução" discutida na Etapa 1.