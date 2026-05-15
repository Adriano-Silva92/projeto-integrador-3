package br.appLogin.appLogin.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.appLogin.appLogin.model.Avaliacao;

public interface AvaliacaoRepository extends CrudRepository<Avaliacao, Long> {
	List<Avaliacao> findByTurmaId(Long turmaId);
	
	void deleteByTurmaId(Long turmaId);

}