package br.com.sistema.acompanhamento.treino;

import br.com.sistema.acompanhamento.exercicio.Exercicio;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
// Garante que a posição de cada exercício seja única dentro do treino.
@Table(name = "item_treino", uniqueConstraints = @UniqueConstraint(name = "uk_item_treino_ordem", columnNames = {"treino_id", "ordem"}))
public class ItemTreino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false)
    private Treino treino;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercicio_id", nullable = false)
    private Exercicio exercicio;
    @Column(nullable = false) private Integer ordem;
    @Column(name = "series_previstas", nullable = false) private Integer seriesPrevistas;
    @Column(name = "reps_previstas", nullable = false) private Integer repsPrevistas;
    @Column(name = "carga_prevista", precision = 10, scale = 2) private BigDecimal cargaPrevista;
    @Column(nullable = false) private boolean concluido;
    @OneToMany(mappedBy = "itemTreino")
    @OrderBy("numeroSerie ASC")
    private List<SerieExecutada> series = new ArrayList<>();

    public Long getId() { return id; }
    public Treino getTreino() { return treino; }
    public void setTreino(Treino treino) { this.treino = treino; }
    public Exercicio getExercicio() { return exercicio; }
    public void setExercicio(Exercicio exercicio) { this.exercicio = exercicio; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public Integer getSeriesPrevistas() { return seriesPrevistas; }
    public void setSeriesPrevistas(Integer seriesPrevistas) { this.seriesPrevistas = seriesPrevistas; }
    public Integer getRepsPrevistas() { return repsPrevistas; }
    public void setRepsPrevistas(Integer repsPrevistas) { this.repsPrevistas = repsPrevistas; }
    public BigDecimal getCargaPrevista() { return cargaPrevista; }
    public void setCargaPrevista(BigDecimal cargaPrevista) { this.cargaPrevista = cargaPrevista; }
    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
    public List<SerieExecutada> getSeries() { return series; }
    public void adicionarSerie(SerieExecutada serie) { series.add(serie); serie.setItemTreino(this); }
}
