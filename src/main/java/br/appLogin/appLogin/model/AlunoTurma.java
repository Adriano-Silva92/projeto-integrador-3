package br.appLogin.appLogin.model;

import jakarta.persistence.*;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"aluno_id", "turma_id"})
    }
)
public class AlunoTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ALUNO
    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    // TURMA
    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    public AlunoTurma() {}

    public AlunoTurma(Aluno aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
    }

    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }
}