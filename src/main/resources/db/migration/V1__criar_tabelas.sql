CREATE TABLE praticante (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_praticante_email UNIQUE (email)
);

CREATE TABLE exercicio (
    id BIGSERIAL PRIMARY KEY,
    praticante_id BIGINT REFERENCES praticante(id),
    nome VARCHAR(120) NOT NULL,
    grupo_muscular VARCHAR(20) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    recorde_carga NUMERIC(10,2),
    recorde_reps_para_carga INTEGER,
    recorde_volume_sessao NUMERIC(12,2),
    total_vezes_realizado INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE treino (
    id BIGSERIAL PRIMARY KEY,
    praticante_id BIGINT NOT NULL REFERENCES praticante(id),
    treino_origem_id BIGINT REFERENCES treino(id),
    identificacao VARCHAR(120) NOT NULL,
    is_modelo BOOLEAN NOT NULL,
    data_treino DATE,
    status VARCHAR(30) NOT NULL,
    iniciado_em TIMESTAMPTZ,
    finalizado_em TIMESTAMPTZ
);

CREATE TABLE item_treino (
    id BIGSERIAL PRIMARY KEY,
    treino_id BIGINT NOT NULL REFERENCES treino(id),
    exercicio_id BIGINT NOT NULL REFERENCES exercicio(id),
    ordem INTEGER NOT NULL,
    series_previstas INTEGER NOT NULL,
    reps_previstas INTEGER NOT NULL,
    carga_prevista NUMERIC(10,2),
    concluido BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_item_treino_ordem UNIQUE (treino_id, ordem)
);

CREATE TABLE serie_executada (
    id BIGSERIAL PRIMARY KEY,
    item_treino_id BIGINT NOT NULL REFERENCES item_treino(id),
    numero_serie INTEGER NOT NULL,
    carga_utilizada NUMERIC(10,2) NOT NULL,
    repeticoes_realizadas INTEGER NOT NULL,
    horario_conclusao TIMESTAMPTZ,
    volume_calculado NUMERIC(12,2) GENERATED ALWAYS AS (carga_utilizada * repeticoes_realizadas) STORED,
    CONSTRAINT uk_serie_numero UNIQUE (item_treino_id, numero_serie)
);

CREATE INDEX idx_exercicio_praticante ON exercicio(praticante_id);
CREATE INDEX idx_treino_praticante ON treino(praticante_id);
CREATE INDEX idx_treino_origem ON treino(treino_origem_id);
