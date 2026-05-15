package br.appLogin.appLogin.model;

import java.util.List;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ra;

    // RELACIONAMENTO COM USUÁRIO
    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    // NOTAS DO ALUNO
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Nota> notas;

    public Aluno() {}

    public Aluno(String ra, Usuario usuario) {
        this.ra = ra;
        this.usuario = usuario;
    }

    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    public String getRa() {
        return ra;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Nota> getNotas() {
        return notas;
    }

    public void setNotas(List<Nota> notas) {
        this.notas = notas;
    }

    // MÉTODOS DE REGRA DE NEGÓCIO

    public double mediaNotas() {
        if (notas == null || notas.isEmpty()) return 0.0;
        return notas.stream()
                .mapToDouble(Nota::getValor)
                .average()
                .orElse(0.0);
    }

    public String getSituacao() {
        return mediaNotas() >= 6.0 ? "Aprovado" : "Reprovado";
    }
}