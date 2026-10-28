package br.com.sistema.acompanhamento.historico.domain;

import java.math.BigDecimal;

public record ComparacaoVolumeDecimal(BigDecimal volumeSessao1, BigDecimal volumeSessao2,
                                      BigDecimal diferenca) { }
