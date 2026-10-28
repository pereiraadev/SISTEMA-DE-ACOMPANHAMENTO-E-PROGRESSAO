# Guia de integração do JavaFX

O backend usa a URL configurada em `SERVER_PORT` (porta 8080 por padrão). O JavaFX deve manter essa URL em configuração própria, por exemplo `http://localhost:8080`, sem fixá-la no código. O contrato completo está em `contrato-api.json`.

Para iniciar o backend, configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` e `JWT_SECRET` no ambiente. O segredo JWT precisa ter ao menos 32 bytes. Depois execute `mvn spring-boot:run` na raiz do repositório. A inicialização com Flyway aplica as migrations pendentes ao banco configurado; use um banco preparado para essa finalidade.

## Fluxo

1. Cadastre o praticante com `POST /auth/cadastrar`. O cadastro devolve ID, nome e e-mail; nunca devolve a senha.
2. Faça login com `POST /auth/login`. Guarde o campo `token` apenas durante a sessão da aplicação.
3. Envie `Authorization: Bearer <token>` em todas as rotas `/api/...` e `/treinos/historico`. Token ausente ou inválido recebe HTTP 401. Novo login é necessário após a expiração.
4. Consulte `GET /api/exercicios` ou cadastre um exercício próprio com `POST /api/exercicios`. Crie um modelo em `POST /api/modelos` ou consulte `GET /api/modelos`.
5. Inicie o modelo em `POST /api/modelos/{id}/iniciar`. Guarde o ID da execução retornada e os IDs dos itens.
6. Consulte `GET /api/execucoes/{id}`. Registre cada série com `POST /api/execucoes/{id}/itens/{itemId}/series`, numerando de 1 até o total previsto. Enquanto a execução estiver em andamento, `PUT` na rota da série corrige carga e repetições, mantendo o número.
7. Finalize com `POST /api/execucoes/{id}/finalizar`. É necessário registrar ao menos uma série. Uma segunda finalização recebe HTTP 409.
8. Consulte `GET /api/historico`, `GET /api/historico/{id}/anterior?exercicioId=7`, `GET /api/historico/comparar`, `GET /api/exercicios/{id}/recordes`, `GET /api/exercicios/{id}/volume`, `GET /api/exercicios/{id}/maior-reps?carga=40.5` e `GET /api/dashboard`.

Para um resumo ordenável das mesmas execuções, use `GET /treinos/historico?ordem=data|carga|repeticoes|volume`. O padrão é `data`. Essa rota devolve `HistoricoTreinoDTO`, enquanto `/api/historico` devolve os treinos com itens e séries.

## Exemplos

Cadastro: `POST /auth/cadastrar` com `{"nome":"Pessoa Exemplo","email":"pessoa@example.com","senha":"senha-de-exemplo"}` retorna HTTP 201 e `{"id":1,"nome":"Pessoa Exemplo","email":"pessoa@example.com"}`.

Login: `POST /auth/login` com `{"email":"pessoa@example.com","senha":"senha-de-exemplo"}` retorna `{"token":"<jwt>","tipo":"Bearer","praticante":{"id":1,"nome":"Pessoa Exemplo","email":"pessoa@example.com"}}`.

Modelo: `POST /api/modelos` com `{"identificacao":"Treino A","itens":[{"exercicioId":7,"ordem":1,"seriesPrevistas":3,"repsPrevistas":10,"cargaPrevista":40.00}]}`. Use apenas IDs retornados por `/api/exercicios`.

Exercício próprio: `POST /api/exercicios` com `{"nome":"Supino reto","grupoMuscular":"PEITO","descricao":"Barra livre"}`. Os grupos aceitos são `PEITO`, `COSTAS`, `PERNAS`, `OMBROS` e `BRACOS`.

Série: `POST /api/execucoes/12/itens/30/series` com `{"numeroSerie":1,"carga":40.50,"repeticoes":10}` retorna HTTP 201 e campos `id`, `numeroSerie`, `carga`, `repeticoes`, `volume`.

Erros de negócio e validação usam `{"codigo":"ESTADO_INVALIDO","mensagem":"..."}`. Dados inválidos recebem HTTP 400; recurso inexistente ou de outro praticante recebe 404; duplicidade ou transição inválida recebe 409. As rotas de autenticação devolvem 401 para credenciais inválidas.

## Regras importantes

`MODELO` é uma ficha. `EM_ANDAMENTO` é uma execução editável. `CONCLUIDO` é uma execução fechada e aparece no histórico. O backend calcula volumes com carga e repetições executadas. Recordes pessoais consultados em `/recordes` são derivados apenas das execuções concluídas do praticante. A interface não deve considerar o retorno do cadastro como login; obtenha o JWT na rota de login.

Os dois endpoints de histórico incluem somente execuções concluídas com `finalizadoEm` preenchido. A ordem cronológica usa o instante de finalização em UTC, do mais antigo ao mais recente, e desempata pelo ID crescente. Treinos legados concluídos sem `finalizadoEm` ficam fora do histórico e das métricas derivadas até uma regularização explícita dos dados; o backend não infere essa data do início ou da data do treino.

`repeticoesNaMaiorCarga` indica o maior número de repetições entre as séries feitas com a maior carga registrada. Para exercícios próprios, os campos de recorde da entidade são atualizados na finalização; para exercícios globais, o backend calcula os recordes por praticante na consulta e não grava marcas pessoais no registro compartilhado. O `volume_calculado` do banco é somente leitura e gerado pelo PostgreSQL.
