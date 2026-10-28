package br.com.sistema.acompanhamento.treino;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "serie_executada", uniqueConstraints = @UniqueConstraint(name = "uk_serie_numero", columnNames = {"item_treino_id", "numero_serie"}))
public class SerieExecutada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_treino_id", nullable = false)
    private ItemTreino itemTreino;
    @Column(name = "numero_serie", nullable = false) private Integer numeroSerie;
    @Column(name = "carga_utilizada", nullable = false, precision = 10, scale = 2) private BigDecimal cargaUtilizada;
    @Column(name = "repeticoes_realizadas", nullable = false) private Integer repeticoesRealizadas;
    @Column(name = "horario_conclusao") private OffsetDateTime horarioConclusao;
    // O PostgreSQL calcula este valor a partir da carga e das repetições.
    // Como a coluna é GENERATED STORED, o JPA apenas lê o resultado do banco.
    @Column(name = "volume_calculado", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal volumeCalculado;

    public Long getId() { return id; }
    public ItemTreino getItemTreino() { return itemTreino; }
    public void setItemTreino(ItemTreino itemTreino) { this.itemTreino = itemTreino; }
    public Integer getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(Integer numeroSerie) { this.numeroSerie = numeroSerie; }
    public BigDecimal getCargaUtilizada() { return cargaUtilizada; }
    public void setCargaUtilizada(BigDecimal cargaUtilizada) { this.cargaUtilizada = cargaUtilizada; }
    public Integer getRepeticoesRealizadas() { return repeticoesRealizadas; }
    public void setRepeticoesRealizadas(Integer repeticoesRealizadas) { this.repeticoesRealizadas = repeticoesRealizadas; }
    public OffsetDateTime getHorarioConclusao() { return horarioConclusao; }
    public void setHorarioConclusao(OffsetDateTime horarioConclusao) { this.horarioConclusao = horarioConclusao; }
    public BigDecimal getVolumeCalculado() { return volumeCalculado; }
}
