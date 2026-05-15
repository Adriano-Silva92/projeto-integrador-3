package br.appLogin.appLogin.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(
    name = "frequencias",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"aluno_id", "data"})
    }
)
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIONAMENTO COM ALUNO
    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    // RELACIONAMENTO COM TURMA
    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    // DATA DA AULA
    @Column(nullable = false)
    private LocalDate data;

    // STATUS DA PRESENÇA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFrequencia status;

    // ENUM (DENTRO DA CLASSE)
    public enum StatusFrequencia {
        PRESENTE,
        FALTA,
        JUSTIFICADA
    }

    // GETTERS E SETTERS
    
    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public StatusFrequencia getStatus() {
        return status;
    }

    public void setStatus(StatusFrequencia status) {
        this.status = status;
    }
}