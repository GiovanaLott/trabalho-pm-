package com.hospital.repositorios;

import com.hospital.entidades.Internacao;
import com.hospital.entidades.StatusInternacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    List<Internacao> findByPacienteId(Long pacienteId);

    List<Internacao> findByQuartoIdAndStatus(Long quartoId, StatusInternacao status);

    boolean existsByPacienteIdAndStatus(Long pacienteId, StatusInternacao status);

    List<Internacao> findByStatus(StatusInternacao status);
}
