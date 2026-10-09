package com.hospital.repositorios;

import com.hospital.entidades.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);

    Optional<ProfissionalSaude> findByRegistroProfissional(String registroProfissional);

    /** Lista os profissionais de uma especialidade (sem diferenciar maiusculas), em ordem alfabetica. */
    List<ProfissionalSaude> findByEspecialidadeIgnoreCaseOrderByNomeAsc(String especialidade);
}
