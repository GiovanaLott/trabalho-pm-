package com.hospital.repositorios;

import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.StatusCirurgia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedimentoCirurgicoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {

    List<ProcedimentoCirurgico> findByPacienteId(Long pacienteId);

    @Query("SELECT p FROM ProcedimentoCirurgico p WHERE p.profissional.id = :cirurgiaoId")
    List<ProcedimentoCirurgico> findByCirurgiaoId(@Param("cirurgiaoId") Long cirurgiaoId);

    List<ProcedimentoCirurgico> findBySituacao(StatusCirurgia situacao);

    boolean existsByPacienteId(Long pacienteId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProcedimentoCirurgico p WHERE p.profissional.id = :cirurgiaoId")
    boolean existsByCirurgiaoId(@Param("cirurgiaoId") Long cirurgiaoId);
}
