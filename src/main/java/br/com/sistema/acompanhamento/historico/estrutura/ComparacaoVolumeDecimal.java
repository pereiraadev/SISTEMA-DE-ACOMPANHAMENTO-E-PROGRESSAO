package br.com.sistema.acompanhamento.historico.estrutura;

import java.math.BigDecimal;

public record ComparacaoVolumeDecimal(BigDecimal volumeSessao1, BigDecimal volumeSessao2,
                                      BigDecimal diferenca) { }
