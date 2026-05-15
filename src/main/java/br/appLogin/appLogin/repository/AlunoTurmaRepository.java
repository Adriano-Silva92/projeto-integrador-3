package br.appLogin.appLogin.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.appLogin.appLogin.model.AlunoTurma;

public interface AlunoTurmaRepository extends CrudRepository<AlunoTurma, Long> {

    List<AlunoTurma> findByTurmaId(Long turmaId);

    List<AlunoTurma> findByAlunoId(Long alunoId);
    
    void deleteByAlunoId(Long alunoId);
    void deleteByTurmaId(Long turmaId);
}