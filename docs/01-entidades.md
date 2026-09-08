-Identificacao das identidades

Praticante (usuário do sistema)
id, nome, email, senha, data_cadastro

//Nota de segurança a incluir no relatório: senha deve ser armazenada com hash (ex: bcrypt), nunca em texto puro.

GrupoMuscular
id, nome

Atende: consulta de desempenho histórico por grupo muscular.

Exercicio
id, praticante_id (opcional — exercícios customizados), grupo_muscular_id, nome, descricao, equipamento, ativo (booleano)

O campo ativo permite que um exercício saia da ficha atual sem apagar seu histórico.

Treino (modelo/ficha, ex: "Treino A")
id, praticante_id, nome, descricao, ativo

Representa a intenção/rotina planejada, não a execução.

ItemTreinoModelo (entidade associativa Treino–Exercício)
id, treino_id, exercicio_id, ordem_execucao, series_planejadas, repeticoes_alvo, tempo_descanso_segundos, observacao_tecnica

Resolve a relação N:N entre Treino e Exercício.

SessaoTreino (execução real, em uma data)
id, praticante_id, treino_id (opcional), data_hora_inicio, data_hora_fim, observacoes, status (EM_ANDAMENTO, CONCLUIDO)

Representa o histórico — imutável após concluído.

ItemTreino (exercício efetivamente executado dentro de uma sessão)
id, sessao_treino_id, exercicio_id, ordem_execucao, volume_total_exercicio (calculado)

SerieRealizada
id, item_treino_id, numero_serie, carga, repeticoes, volume_serie (calculado: carga × repeticoes), concluida (booleano), data_hora_execucao

RecordePessoal
id, praticante_id, exercicio_id, serie_realizada_id, tipo_recorde (MAIOR_CARGA, MAIS_REPETICOES_POR_CARGA, MAIOR_VOLUME), valor_carga, repeticoes, data_conquista

- Relações entre Entidades

Praticante possui muitos Treinos (fichas)
Praticante possui muitas SessõesTreino (histórico de execuções)
Praticante possui muitos RecordesPessoais
GrupoMuscular possui muitos Exercícios
Treino possui muitos ItensTreinoModelo
Exercício possui muitos ItensTreinoModelo (resolve o antigo N:N Treino–Exercício)
SessaoTreino possui muitos ItensTreino
Exercício possui muitos ItensTreino (ao longo de diferentes sessões, ao longo do tempo)
ItemTreino possui muitas SériesRealizadas
SerieRealizada pode originar um RecordePessoal (1:1 opcional)