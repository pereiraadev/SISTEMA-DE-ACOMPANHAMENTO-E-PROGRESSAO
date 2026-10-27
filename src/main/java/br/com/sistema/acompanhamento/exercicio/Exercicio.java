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
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public BigDecimal getRecordeCarga() { return recordeCarga; }
    public Integer getRecordeRepsParaCarga() { return recordeRepsParaCarga; }
    public BigDecimal getRecordeVolumeSessao() { return recordeVolumeSessao; }
    public Integer getTotalVezesRealizado() { return totalVezesRealizado; }

    public void atualizarRecordes(BigDecimal carga, Integer repeticoes, BigDecimal volume) {
        if (carga != null && (recordeCarga == null || carga.compareTo(recordeCarga) > 0)) {
            recordeCarga = carga;
        }
        if (repeticoes != null && (recordeRepsParaCarga == null || repeticoes > recordeRepsParaCarga)) {
            recordeRepsParaCarga = repeticoes;
        }
        if (volume != null && (recordeVolumeSessao == null || volume.compareTo(recordeVolumeSessao) > 0)) {
            recordeVolumeSessao = volume;
        }
        totalVezesRealizado = totalVezesRealizado == null ? 1 : totalVezesRealizado + 1;
    }
}
