package br.com.sistema.acompanhamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "serie_executada")
public class SerieExecutada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_treino_id")
    private ItemTreino itemTreino;

    @Column(name = "numero_serie", nullable = false)
    private int numeroSerie;

    @Column(name = "carga_utilizada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cargaUtilizada;

    @Column(name = "repeticoes_realizadas", nullable = false)
    private int repeticoesRealizadas;

    // Essa coluna o postgres calcula sozinho (generated), por isso nao grava daqui.
    @Column(name = "volume_calculado", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal volumeCalculado;

    protected SerieExecutada() { }

    public Long getId() { return id; }
    public ItemTreino getItemTreino() { return itemTreino; }
    public int getNumeroSerie() { return numeroSerie; }
    public BigDecimal getCargaUtilizada() { return cargaUtilizada; }
    public int getRepeticoesRealizadas() { return repeticoesRealizadas; }
    public BigDecimal getVolumeCalculado() { return volumeCalculado; }

    // Volume da serie = carga x repeticoes.
    // Se o banco ainda nao calculou (serie nova na memoria) calcula aqui.
    public BigDecimal calcularVolume() {
        if (getVolumeCalculado() != null) {
            return getVolumeCalculado();
        }
        BigDecimal carga = getCargaUtilizada();
        int repeticoes = getRepeticoesRealizadas();
        if (carga == null || carga.signum() < 0 || repeticoes < 0) {
            throw new IllegalStateException("Série com carga ou repetições inválidas.");
        }
        return carga.multiply(BigDecimal.valueOf(repeticoes));
    }
}
