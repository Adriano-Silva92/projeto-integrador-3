package br.appLogin.appLogin.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoAvaliacao tipo;

    private LocalDate dataCriacao;

    private LocalDate dataVencimento;

    private Double peso;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL)
    private List<Nota> notas;

    public Avaliacao() {}

    // GETTERS E SETTERS

    public Long getId() { return id; }

    public String getNome() { return nome; }

    public TipoAvaliacao getTipo() { return tipo; }

    public LocalDate getDataCriacao() { return dataCriacao; }

    public LocalDate getDataVencimento() { return dataVencimento; }

    public Double getPeso() { return peso; }

    public Turma getTurma() { return turma; }

    public List<Nota> getNotas() { return notas; }

    public void setId(Long id) { this.id = id; }

    public void setNome(String nome) { this.nome = nome; }

    public void setTipo(TipoAvaliacao tipo) { this.tipo = tipo; }

    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public void setPeso(Double peso) { this.peso = peso; }

    public void setTurma(Turma turma) { this.turma = turma; }

    public void setNotas(List<Nota> notas) { this.notas = notas; }
}