package com.hospital.repositorios;

import com.hospital.entidades.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);

    Optional<Paciente> findByCpf(String cpf);

    /** Busca pacientes cujo nome contenha o texto informado (sem diferenciar maiusculas), em ordem alfabetica. */
    List<Paciente> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
