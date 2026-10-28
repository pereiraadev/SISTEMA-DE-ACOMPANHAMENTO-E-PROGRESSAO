package br.com.sistema.acompanhamento.exercicio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// so os campos que o historico e o dashboard usam
@Entity
@Table(name = "exercicio")
public class Exercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "grupo_muscular", nullable = false, length = 20)
    private String grupoMuscular;

    protected Exercicio() { }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getGrupoMuscular() { return grupoMuscular; }
}
