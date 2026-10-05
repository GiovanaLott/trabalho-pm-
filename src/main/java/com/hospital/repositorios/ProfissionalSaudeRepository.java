package com.hospital.repositorios;

import com.hospital.entidades.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);

    Optional<ProfissionalSaude> findByRegistroProfissional(String registroProfissional);
}
