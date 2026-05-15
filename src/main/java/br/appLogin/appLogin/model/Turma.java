package br.appLogin.appLogin.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: 1º Ano A

    private String nivel; // Fundamental ou Médio

    // RELACIONAMENTO COM PROFESSOR
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    // LISTA AVALIAÇÕES DA TURMA
    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;

    public Turma() {}

    public Turma(String nome, String nivel, Professor professor) {
        this.nome = nome;
        this.nivel = nivel;
        this.professor = professor;
    }

    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNivel() {
        return nivel;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<Avaliacao> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}