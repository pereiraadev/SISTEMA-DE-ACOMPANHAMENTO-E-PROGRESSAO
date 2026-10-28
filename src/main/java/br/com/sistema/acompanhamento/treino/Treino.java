package br.com.sistema.acompanhamento.treino;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "treino")
public class Treino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "praticante_id", nullable = false)
    private Long praticanteId;

    @Column(nullable = false, length = 120)
    private String identificacao;

    @Column(name = "is_modelo", nullable = false)
    private boolean modelo;

    @Column(name = "data_treino")
    private LocalDate dataTreino;

    @Column(nullable = false, length = 30)
    private String status; // EM_ANDAMENTO ou CONCLUIDO

    @OneToMany(mappedBy = "treino")
    @OrderBy("ordem")
    @BatchSize(size = 50)
    private List<ItemTreino> itens = new ArrayList<>();

    protected Treino() { }

    public Long getId() { return id; }
    public Long getPraticanteId() { return praticanteId; }
    public String getIdentificacao() { return identificacao; }
    public boolean isModelo() { return modelo; }
    public LocalDate getDataTreino() { return dataTreino; }
    public String getStatus() { return status; }
    public List<ItemTreino> getItens() { return itens; }

    // volume do treino = somatoria do volume de cada exercicio
    public BigDecimal calcularVolumeTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemTreino item : getItens()) {
            total = total.add(item.calcularVolumeTotal());
        }
        return total;
    }
}
