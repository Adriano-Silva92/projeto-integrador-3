package br.appLogin.appLogin.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import br.appLogin.appLogin.model.Professor;

public interface ProfessorRepository extends CrudRepository<Professor, Long> {

    Optional<Professor> findByUsuarioId(Long usuarioId);

}