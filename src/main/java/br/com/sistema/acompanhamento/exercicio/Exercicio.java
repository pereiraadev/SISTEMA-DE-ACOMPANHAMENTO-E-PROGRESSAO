package br.com.sistema.acompanhamento.exercicio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import br.com.sistema.acompanhamento.praticante.Praticante;

@Entity
@Table(name = "exercicio")
public class Exercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praticante_id")
    private Praticante praticante;
    @Column(length = 120, nullable = false) private String nome;
    @Column(name = "grupo_muscular", length = 20, nullable = false) private String grupoMuscular;
    @Column(length = 500) private String descricao;
    @Column(nullable = false) private boolean ativo = true;
    // Recordes mantidos para permitir consultas rápidas de desempenho.
    @Column(name = "recorde_carga", precision = 10, scale = 2) private BigDecimal recordeCarga;
    @Column(name = "recorde_reps_para_carga") private Integer recordeRepsParaCarga;
    @Column(name = "recorde_volume_sessao", precision = 12, scale = 2) private BigDecimal recordeVolumeSessao;
    @Column(name = "total_vezes_realizado", nullable = false) private Integer totalVezesRealizado = 0;
    public Long getId() { return id; }
    public Praticante getPraticante() { return praticante; }
    public void setPraticante(Praticante praticante) { this.praticante = praticante; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public BigDecimal getRecordeCarga() { return recordeCarga; }
    public void setRecordeCarga(BigDecimal valor) { this.recordeCarga = valor; }
    public Integer getRecordeRepsParaCarga() { return recordeRepsParaCarga; }
    public void setRecordeRepsParaCarga(Integer valor) { this.recordeRepsParaCarga = valor; }
    public BigDecimal getRecordeVolumeSessao() { return recordeVolumeSessao; }
    public void setRecordeVolumeSessao(BigDecimal valor) { this.recordeVolumeSessao = valor; }
    public Integer getTotalVezesRealizado() { return totalVezesRealizado; }
    public void setTotalVezesRealizado(Integer valor) { this.totalVezesRealizado = valor; }

    public void atualizarRecordes(BigDecimal carga, Integer repeticoes, BigDecimal volume) {
        if (carga != null && repeticoes != null) {
            if (recordeCarga == null || carga.compareTo(recordeCarga) > 0) {
                recordeCarga = carga;
                recordeRepsParaCarga = repeticoes;
            } else if (carga.compareTo(recordeCarga) == 0
                    && (recordeRepsParaCarga == null || repeticoes > recordeRepsParaCarga)) {
                recordeRepsParaCarga = repeticoes;
            }
        }
        if (volume != null && (recordeVolumeSessao == null || volume.compareTo(recordeVolumeSessao) > 0)) {
            recordeVolumeSessao = volume;
        }
        totalVezesRealizado = totalVezesRealizado == null ? 1 : totalVezesRealizado + 1;
    }
}
