package br.appLogin.appLogin.model;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = {"aluno_id", "avaliacao_id"})
	    }
	)
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valor;

    private LocalDate dataLancamento;

    // RELACIONAMENTO COM ALUNO
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    // RELACIONAMENTO COM AVALIAÇÃO
    @ManyToOne
    @JoinColumn(name = "avaliacao_id")
    private Avaliacao avaliacao;

    public Nota() {}

    public Nota(Double valor, Aluno aluno, Avaliacao avaliacao, LocalDate dataLancamento) {
        this.valor = valor;
        this.aluno = aluno;
        this.avaliacao = avaliacao;
        this.dataLancamento = dataLancamento;
    }

    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    public Double getValor() {
        return valor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }
}