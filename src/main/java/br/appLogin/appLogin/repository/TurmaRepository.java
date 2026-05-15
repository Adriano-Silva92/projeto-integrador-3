package br.appLogin.appLogin.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.appLogin.appLogin.model.Turma;

public interface TurmaRepository extends CrudRepository<Turma, Long> {

    List<Turma> findByProfessorId(Long professorId);

}