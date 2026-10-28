-- Acelera consultas de exercícios filtradas ou ordenadas pelo recorde de carga.
CREATE INDEX idx_exercicio_recorde_carga ON exercicio(recorde_carga);
-- Acelera consultas baseadas na quantidade de repetições do recorde.
CREATE INDEX idx_exercicio_recorde_reps ON exercicio(recorde_reps_para_carga);
-- Acelera consultas baseadas no maior volume obtido em uma sessão.
CREATE INDEX idx_exercicio_recorde_volume ON exercicio(recorde_volume_sessao);
