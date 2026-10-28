package br.com.sistema.acompanhamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "item_treino")
public class ItemTreino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id")
    private Treino treino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercicio_id")
    private Exercicio exercicio;

    @Column(nullable = false)
    private int ordem;

    @OneToMany(mappedBy = "itemTreino")
    @OrderBy("numeroSerie")
    @BatchSize(size = 50)
    private List<SerieExecutada> series = new ArrayList<>();

    protected ItemTreino() { }

    public Long getId() { return id; }
    public Treino getTreino() { return treino; }
    public Exercicio getExercicio() { return exercicio; }
    public int getOrdem() { return ordem; }
    public List<SerieExecutada> getSeries() { return series; }

    // Volume do exercicio = somatoria do volume das series.
    public BigDecimal calcularVolumeTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (SerieExecutada serie : getSeries()) {
            total = total.add(serie.calcularVolume());
        }
        return total;
    }

    // Maior carga usada nas series (zero se nao tem serie)
    public BigDecimal obterMaiorCarga() {
        BigDecimal maior = BigDecimal.ZERO;
        for (SerieExecutada serie : getSeries()) {
            if (serie.getCargaUtilizada().compareTo(maior) > 0) {
                maior = serie.getCargaUtilizada();
            }
        }
        return maior;
    }

    // Total de repeticoes somando todas as series.
    public int contarRepeticoes() {
        int total = 0;
        for (SerieExecutada serie : getSeries()) {
            total += serie.getRepeticoesRealizadas();
        }
        return total;
    }
}
