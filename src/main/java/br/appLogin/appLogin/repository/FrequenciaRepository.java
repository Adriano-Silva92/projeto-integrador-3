package br.appLogin.appLogin.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import br.appLogin.appLogin.model.Frequencia;

public interface FrequenciaRepository extends JpaRepository<Frequencia, Long> {

    Optional<Frequencia> findByAlunoIdAndData(Long alunoId, LocalDate data);

    List<Frequencia> findByTurmaIdAndData(Long turmaId, LocalDate data);

    @Modifying
    @Transactional
    @Query("DELETE FROM Frequencia f WHERE f.turma.id = :turmaId")
    void deleteByTurmaId(Long turmaId);
    
    long countByTurmaIdAndStatus(
            Long turmaId,
            Frequencia.StatusFrequencia status
    );

    long countByTurmaIdAndStatusAndData(
            Long turmaId,
            Frequencia.StatusFrequencia status,
            LocalDate data
    );
}