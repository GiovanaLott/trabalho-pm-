package com.hospital.repositorios;

import com.hospital.entidades.Consulta;
import com.hospital.entidades.SituacaoConsulta;
import com.hospital.entidades.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteId(Long pacienteId);

    List<Consulta> findByProfissionalId(Long profissionalId);

    List<Consulta> findBySituacao(SituacaoConsulta situacao);

    @Query("SELECT c FROM Consulta c WHERE TYPE(c) = com.hospital.entidades.AtendimentoEmergencia AND c.situacao IN (com.hospital.entidades.SituacaoConsulta.AGENDADA, com.hospital.entidades.SituacaoConsulta.CONFIRMADA)")
    List<Consulta> findEmergenciasAguardandoAvaliacao();

    @Query("SELECT c FROM Consulta c WHERE TYPE(c) = :tipo")
    List<Consulta> findByTipoConsulta(@Param("tipo") Class<? extends Consulta> tipo);

    @Query("SELECT c FROM Consulta c WHERE c.profissional.id = :profissionalId AND c.situacao <> com.hospital.entidades.SituacaoConsulta.CANCELADA AND c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findConflitoHorarioProfissional(@Param("profissionalId") Long profissionalId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim,
                                                   @Param("statusIgnorado") StatusConsulta statusIgnorado);

    @Query("SELECT c FROM Consulta c WHERE c.profissional.id = :profissionalId AND c.situacao <> com.hospital.entidades.SituacaoConsulta.CANCELADA AND c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findConflitoHorarioProfissional(@Param("profissionalId") Long profissionalId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim);
}
