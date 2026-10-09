package com.hospital.repositorios;

import com.hospital.entidades.Internacao;
import com.hospital.entidades.StatusInternacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    List<Internacao> findByPacienteId(Long pacienteId);

    List<Internacao> findByQuartoIdAndStatus(Long quartoId, StatusInternacao status);

    boolean existsByPacienteIdAndStatus(Long pacienteId, StatusInternacao status);

    List<Internacao> findByStatus(StatusInternacao status);

    /** Historico de internacoes do paciente, da mais recente para a mais antiga. */
    List<Internacao> findByPacienteIdOrderByDataEntradaDesc(Long pacienteId);

    /** Internacao atualmente ativa (ou outro status) de um paciente, se houver. */
    Optional<Internacao> findFirstByPacienteIdAndStatus(Long pacienteId, StatusInternacao status);

    /** Quantidade de internacoes de um quarto em determinado status (ex.: EM_ANDAMENTO = leitos ocupados). */
    long countByQuartoIdAndStatus(Long quartoId, StatusInternacao status);
}
