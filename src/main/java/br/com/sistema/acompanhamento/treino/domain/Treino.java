package br.com.sistema.acompanhamento.treino.domain;

import br.com.sistema.acompanhamento.praticante.domain.Praticante;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treino")
public class Treino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "praticante_id", nullable = false)
    private Praticante praticante;
    // Permite rastrear se este treino foi criado a partir de outro treino ou modelo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treino_origem_id")
    private Treino treinoOrigem;
    @Column(nullable = false, length = 120) private String identificacao;
    // Diferencia um modelo reutilizável de uma execução concreta de treino.
    @Column(name = "is_modelo", nullable = false) private boolean modelo;
    @Column(name = "data_treino") private LocalDate dataTreino;
    @Column(nullable = false, length = 30) private String status;
    @Column(name = "iniciado_em") private OffsetDateTime iniciadoEm;
    @Column(name = "finalizado_em") private OffsetDateTime finalizadoEm;
    @OneToMany(mappedBy = "treino", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemTreino> itens = new ArrayList<>();

    public Long getId() { return id; }
    public Praticante getPraticante() { return praticante; }
    public void setPraticante(Praticante praticante) { this.praticante = praticante; }
    public Treino getTreinoOrigem() { return treinoOrigem; }
    public void setTreinoOrigem(Treino treinoOrigem) { this.treinoOrigem = treinoOrigem; }
    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }
    public boolean isModelo() { return modelo; }
    public void setModelo(boolean modelo) { this.modelo = modelo; }
    public LocalDate getDataTreino() { return dataTreino; }
    public void setDataTreino(LocalDate dataTreino) { this.dataTreino = dataTreino; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getIniciadoEm() { return iniciadoEm; }
    public void setIniciadoEm(OffsetDateTime iniciadoEm) { this.iniciadoEm = iniciadoEm; }
    public OffsetDateTime getFinalizadoEm() { return finalizadoEm; }
    public void setFinalizadoEm(OffsetDateTime finalizadoEm) { this.finalizadoEm = finalizadoEm; }
    public List<ItemTreino> getItens() { return itens; }
    public void adicionarItem(ItemTreino item) { itens.add(item); item.setTreino(this); }
}
