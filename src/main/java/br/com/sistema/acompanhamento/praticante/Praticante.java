package br.com.sistema.acompanhamento.praticante;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

// Entidade que representa o usuário do sistema e seus dados de autenticação.
@Entity
// A constraint impede o cadastro de dois praticantes com o mesmo e-mail.
@Table(name = "praticante", uniqueConstraints = @UniqueConstraint(name = "uk_praticante_email", columnNames = "email"))
public class Praticante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String nome;
    @Column(nullable = false, length = 254) private String email;
    @Column(name = "senha", nullable = false, length = 255) private String senha;
    @Column(name = "criado_em", nullable = false, updatable = false) private OffsetDateTime criadoEm;

    @PrePersist void aoCriar() { criadoEm = OffsetDateTime.now(); }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
