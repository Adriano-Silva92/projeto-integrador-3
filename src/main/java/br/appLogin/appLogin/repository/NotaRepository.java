package br.appLogin.appLogin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.appLogin.appLogin.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    Optional<Nota> findByAlunoIdAndAvaliacaoId(Long alunoId, Long avaliacaoId);

    List<Nota> findByAvaliacaoId(Long avaliacaoId);

    List<Nota> findByAlunoId(Long alunoId);
    
    List<Nota> findByAlunoIdAndAvaliacaoTurmaId(Long alunoId, Long turmaId);
    
}