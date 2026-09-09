-> Escolha da Estrutura de Dados

Para cada coleção de dados relevante do sistema, foi realizada uma análise baseada em três critérios: (i) a operação mais frequente sobre a coleção, (ii) se a ordem de inserção possui relevância para alguma regra de negócio, e (iii) se há necessidade de priorização/ordenação por critérios distintos da ordem de chegada. A estrutura de dados foi escolhida com base no comportamento resultante dessa análise, e não por preferência arbitrária do grupo.

-Séries dentro de um ItemTreino

**Estrutura escolhida: ArrayList (Lista - array dinâmico)**

Durante a execução de um treino, as séries de um exercício são inseridas sempre ao final, seguindo rigorosamente a ordem de execução (1ª, 2ª, 3ª série, e assim por diante) — o praticante nunca insere uma série no meio da sequência já registrada, nem reordena séries já concluídas. Não há necessidade de acesso aleatório a uma série específica fora da ordem sequencial.

O ArrayList foi escolhido em vez da Lista Encadeada (LinkedList) porque, nesse cenário, não existe ganho algum em usar uma estrutura mais flexível (que permite inserção/remoção em qualquer posição) para um comportamento que só precisa de inserção no fim. Além disso, o ArrayList oferece obtenção do tamanho da coleção em tempo O(1), o que é utilizado diretamente para atender ao requisito de "visualizar quantas séries já foram concluídas" durante o treino, sem necessidade de percorrer e contar elemento por elemento.

-Recordes Pessoais (histórico de progressão por exercício)

**Estrutura escolhida: Pilha (Stack)**

A operação mais frequente sobre essa coleção não é a inserção (que só ocorre quando um recorde é efetivamente quebrado), e sim a consulta ao recorde atual, que precisa ser comparada a cada nova série registrada pelo praticante. Como o sistema só precisa saber o maior valor já alcançado — e não realiza busca ou remoção em posições intermediárias — a inserção e a leitura ocorrem sempre em uma única extremidade da coleção.

Esse comportamento corresponde exatamente à operação de uma Pilha: cada novo recorde é empilhado (`push`) no topo, e o recorde atual é obtido através da leitura do topo (`peek`), ambas as operações em tempo O(1). Usar uma lista encadeada genérica aqui seria adotar uma estrutura mais poderosa (que permite inserir/remover em qualquer posição) do que o necessário, complicando desnecessariamente uma operação simples. Como benefício adicional, a Pilha preserva naturalmente o histórico de progressão do praticante em um exercício (ex.: 70kg → 75kg → 80kg → 85kg), podendo ser percorrida do topo para a base para exibir a evolução de recordes ao longo do tempo.

-Sessões de Treino (histórico completo do praticante)

**Estrutura escolhida: ArrayList, com ordenação sob demanda via Comparator**

Diferentemente dos recordes pessoais, o histórico de sessões de treino não pode ser resolvido apenas com acesso à última posição: o sistema precisa permitir a consulta e reordenação da coleção completa por múltiplos critérios, conforme exigido pelo enunciado ("permitir ordenar o histórico de determinado exercício por data, carga, repetições ou volume"). Uma Pilha, embora ofereça acesso rápido ao elemento mais recente, não atende a esse requisito, pois não permite reorganizar nem percorrer livremente toda a coleção segundo critérios variáveis.

Por esse motivo, optou-se pelo ArrayList, que permite interação completa e ordenação flexível utilizando `Collections.sort()` com diferentes implementações de `Comparator` (por data, carga, repetições ou volume), aplicada sob demanda, apenas quando o usuário solicita uma visualização específica — não havendo necessidade de manter a coleção permanentemente ordenada. Como as sessões são inseridas sempre em ordem cronológica ao final da lista, a obtenção do "treino anterior" (exigida para servir de referência ao iniciar um novo treino) corresponde ao acesso ao último elemento da lista, em tempo O(1).

-Consulta de treinos por exercício realizado

**Estrutura escolhida: ArrayList com busca sequencial (limitação documentada)**

O requisito "permitir consultar todos os treinos em que determinado exercício foi realizado" representa um problema de busca por chave, para o qual a estrutura mais adequada em termos de eficiência seria uma tabela hash (HashMap), mapeando cada Exercício a uma lista dos registros em que ele aparece, com complexidade de acesso O(1).

Como esse conteúdo ainda não foi trabalhado em profundidade na disciplina até o momento da elaboração deste projeto, o grupo optou conscientemente por resolver esse requisito por meio de busca sequencial sobre a estrutura ArrayList já utilizada para o histórico de sessões, com complexidade O(n) por consulta. O grupo está ciente de que essa abordagem apresenta desempenho inferior ao de uma tabela hash em bases de dados de grande volume, e reconhece essa limitação como uma oportunidade futura de otimização.

-Itens do Treino Modelo (ordem de execução da ficha)

**Estrutura escolhida: ArrayList**

Uma ficha de treino (ex.: "Treino A") contém tipicamente um número pequeno de exercícios (entre 3 e 8), organizados em uma ordem de execução definida pelo praticante. Diferentemente da coleção de séries, essa ordem é frequentemente alterada pelo próprio praticante (por exemplo, por orientação de um profissional, ao inverter a prioridade de execução dos exercícios).

Como a reordenação exige o acesso direto a posições específicas da coleção (ex.: trocar o item da posição 1 com o item da posição 3), o ArrayList foi escolhido por oferecer acesso indexado em O(1). Uma lista encadeada, por não permitir acesso direto por índice, exigiria percorrer a estrutura a partir do início a cada operação de reordenação, o que é desnecessário dado o tamanho reduzido dessa coleção.

-Síntese das escolhas

| Coleção | Estrutura | Justificativa central |
|---------|-----------|-----------------------|
| Séries dentro de ItemTreino | ArrayList | Inserção só no fim, nunca reordena, `size()` em O(1) |
| Recordes Pessoais (progressão) | Pilha (Stack) | Inserção e leitura apenas no topo (push/peek), O(1) |
| Sessões de Treino (histórico) | ArrayList + Comparator | Reordenação sob demanda por múltiplos critérios |
| Consulta de treinos por exercício | ArrayList (com ressalva) | Ideal seria HashMap O(1); não implementado por não ter sido estudado ainda |
| Itens do Treino Modelo (ordem da ficha) | ArrayList | Poucos itens, reordenação frequente por posição |

Observa-se que a maioria das coleções do sistema foi resolvida com ArrayList, o que não constitui uma escolha arbitrária ou repetitiva sem critério, mas reflexo do comportamento real dos dados do domínio: coleções predominantemente pequenas, com inserção no fim e, em alguns casos, necessidade de reordenação por índice ou por critério variável. A única exceção significativa foi o histórico de Recordes Pessoais, cujo padrão de acesso (leitura e escrita restritas a uma única extremidade) é estruturalmente diferente das demais coleções, justificando o uso de uma Pilha.