-> Identificação de Perfis/Papéis
- Perfil Único: Praticante

O sistema possui um único perfil de usuário: Praticante.

Justificativa: o enunciado do tema ("Sistema de Acompanhamento e Progressão de Treinos de Musculação") não menciona nenhum outro ator além do praticante em nenhuma de suas funcionalidades — não há menção a administrador, personal trainer, instrutor ou coordenador. Introduzir perfis adicionais seria extrapolar o escopo definido, sem base no enunciado.

- Responsabilidades do Praticante
Cadastrar e gerenciar seus próprios treinos (fichas/modelos);
Cadastrar exercícios customizados (além dos exercícios base do sistema);
Registrar sessões de treino (execuções reais, com data);
Registrar séries realizadas (carga, repetições) durante uma sessão;
Consultar histórico de treinos e desempenho;
Consultar recordes pessoais e estatísticas gerais.

- Dados Base do Sistema (sem perfil administrativo)

O catálogo inicial de GrupoMuscular e Exercicio (ex: "Peitoral", "Supino Reto") é pré-carregado no sistema via massa de dados inicial (seed), sem necessidade de tela ou perfil de administração. O Praticante consome esses dados prontos e pode, adicionalmente, cadastrar exercícios próprios/customizados (usando o campo praticante_id opcional já definido na Etapa 1), sem que isso exija um segundo perfil.