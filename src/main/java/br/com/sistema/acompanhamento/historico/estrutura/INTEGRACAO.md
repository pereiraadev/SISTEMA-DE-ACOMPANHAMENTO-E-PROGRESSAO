# Integração do histórico

`CarregadorHistorico` recebe os treinos em qualquer ordem, remove fichas modelo, elimina referências repetidas e ordena pela data de realização. O método `carregar` cria uma lista nova em cada chamada.

Ainda não existem neste código as entidades e os repositórios oficiais de treino, exercício e série. Quando forem definidos, um adaptador deve implementar `TreinoComSeries` e `SerieHistorico`, consultar as execuções reais e chamar `CarregadorHistorico` na inicialização de um componente Spring com `@PostConstruct`. O adaptador deve fornecer IDs de sessão estáveis, data de realização, séries executadas e grupo muscular de cada exercício.

`GrupoMuscularEnum` é provisório. Antes de conectar a persistência, alinhar seus valores ao enum oficial da equipe, se houver. Não se criou entidade JPA nem migração.

Consultas sem série correspondente retornam `null` em `obterMaiorRepsParaCarga`. `compararVolumeEntreSessoes` retorna `null` se faltar uma sessão ou o exercício em uma delas. Grupos sem série têm volume zero. Volume é a soma de carga multiplicada por repetições; a diferença é sessão 2 menos sessão 1.
