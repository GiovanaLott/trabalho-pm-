package com.hospital.repositorios;

import com.hospital.entidades.Consulta;
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

    List<Consulta> findByStatus(StatusConsulta status);

    boolean existsByProfissionalIdAndDataHoraAndStatusNot(Long profissionalId, LocalDateTime dataHora, StatusConsulta status);

    @Query("SELECT c FROM Consulta c WHERE c.profissional.id = :profissionalId AND c.status <> :statusIgnorado AND c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findConflitoHorarioProfissional(@Param("profissionalId") Long profissionalId,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fim") LocalDateTime fim,
                                                   @Param("statusIgnorado") StatusConsulta statusIgnorado);
}
