package com.hospital.repositorios;

import com.hospital.entidades.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);

    /** Usado na atualização: existe outro paciente (id diferente) com este CPF? */
    boolean existsByCpfAndIdNot(String cpf, Long id);

    Optional<Paciente> findByCpf(String cpf);

    List<Paciente> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    List<Paciente> findAllByOrderByNomeAsc();
}
