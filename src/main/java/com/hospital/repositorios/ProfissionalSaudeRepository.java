package com.hospital.repositorios;

import com.hospital.entidades.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);

    boolean existsByRegistroProfissionalAndIdNot(String registroProfissional, Long id);

    Optional<ProfissionalSaude> findByRegistroProfissional(String registroProfissional);

    @Query("SELECT e FROM Especialista e " +
            "WHERE LOWER(e.especialidade) LIKE LOWER(CONCAT('%', :especialidade, '%')) " +
            "ORDER BY e.nome")
    List<ProfissionalSaude> findEspecialistasByEspecialidadeContainingIgnoreCase(
            @Param("especialidade") String especialidade);
}
