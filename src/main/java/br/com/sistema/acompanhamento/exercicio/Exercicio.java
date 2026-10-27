package br.com.sistema.acompanhamento.exercicio;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exercicio")
public class Exercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 120, nullable = false) private String nome;
    @Column(name = "grupo_muscular", length = 20, nullable = false) private String grupoMuscular;
    @Column(length = 500) private String descricao;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "recorde_carga", precision = 10, scale = 2) private BigDecimal recordeCarga;
    @Column(name = "recorde_reps_para_carga") private Integer recordeRepsParaCarga;
    @Column(name = "recorde_volume_sessao", precision = 12, scale = 2) private BigDecimal recordeVolumeSessao;
    @Column(name = "total_vezes_realizado", nullable = false) private Integer totalVezesRealizado = 0;
    public Long getId() { return id; }
}
