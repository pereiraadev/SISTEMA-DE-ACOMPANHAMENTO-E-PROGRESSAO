-> Identificação das Entidades

Este documento reflete a versão final, após alinhamento entre os integrantes do grupo. Principais mudanças em relação à primeira versão: (1) Treino e ItemTreinoModelo foram unificados — não existem mais como entidades separadas de "modelo" e "execução"; uma única entidade Treino cumpre os dois papéis, diferenciada pelo atributo isModelo; (2) a entidade RecordePessoal foi removida (ver justificativa na Etapa 3, seção 3.2); (3) GrupoMuscular deixou de ser entidade própria e passou a ser um Enum (GrupoMuscularEnum).

- Lista de Entidades e Atributos

 Praticante (usuário do sistema) id, nome, email, senha, criado_em

Exercicio id, praticante_id (opcional — exercícios customizados), nome, grupo_muscular (Enum: PEITO, COSTAS, PERNAS, OMBROS, BRACOS), descricao, ativo, recorde_carga, recorde_reps_para_carga, recorde_volume_sessao, total_vezes_realizado

Os campos de recorde são sobrescritos a cada nova marca superada (ver limitação assumida na Etapa 3).

 Treino (unifica ficha/modelo e execução) id, praticante_id, treino_origem_id (opcional — aponta para o modelo, quando for uma execução), identificacao, is_modelo (booleano), data_treino (opcional — só preenchido em execuções), status, iniciado_em, finalizado_em

Quando is_modelo = true: representa a ficha reutilizável (ex.: "Treino A"), sem data de execução. Quando is_modelo = false: representa uma execução específica, clonada a partir de um modelo (treino_origem_id), com seus próprios ItemTreino também clonados — garantindo que edições futuras na ficha não afetem o histórico já registrado.

 ItemTreino id, treino_id, exercicio_id, ordem, series_previstas, reps_previstas, carga_prevista, concluido

Existe tanto vinculado a um Treino modelo (planejamento) quanto a um Treino de execução (cópia gerada no momento em que o treino é iniciado).
 
 SerieExecutada id, item_treino_id, numero_serie, carga_utilizada, repeticoes_realizadas, horario_conclusao, volume_calculado (gerado: carga × repetições)

- Relações entre Entidades
Praticante possui muitos Treinos (fichas e execuções)
Praticante possui muitos Exercícios customizados
Treino (execução) referencia um Treino (modelo) de origem — auto-relacionamento opcional
Treino possui muitos ItensTreino
Exercício possui muitos ItensTreino (ao longo de diferentes treinos/execuções)
ItemTreino possui muitas SériesExecutadas

- Limitações assumidas conscientemente pelo grupo
Sem histórico detalhado de recordes pessoais: o sistema mantém apenas o valor mais recente de recorde por exercício, sem registrar data ou progressão ao longo do tempo (detalhado na Etapa 3, seção 3.2).