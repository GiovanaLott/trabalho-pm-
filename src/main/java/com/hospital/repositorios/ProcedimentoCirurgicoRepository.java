package com.hospital.repositorios;

import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.StatusCirurgia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedimentoCirurgicoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {

    List<ProcedimentoCirurgico> findByPacienteId(Long pacienteId);

    List<ProcedimentoCirurgico> findByCirurgiaoId(Long cirurgiaoId);

    List<ProcedimentoCirurgico> findBySituacao(StatusCirurgia situacao);
}
