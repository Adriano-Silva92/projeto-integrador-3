package br.appLogin.appLogin.repository;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import br.appLogin.appLogin.model.Aluno;

//REPOSITORY BANCO DE DADOS MODEL ALUNO
public interface AlunoRepository extends CrudRepository<Aluno, Long> {
	Optional<Aluno> findByUsuarioEmail(String email);
}
