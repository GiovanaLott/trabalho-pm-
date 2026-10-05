package com.hospital.servicos;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.entidades.Consulta;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.StatusConsulta;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           PacienteRepository pacienteRepository,
                           ProfissionalSaudeRepository profissionalRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional
    public ConsultaDTO agendarConsulta(ConsultaRequestDTO dto) {
        if (dto.getDataHora() == null) {
            throw new RegraNegocioException("Data e hora da consulta são obrigatórias.");
        }

        if (dto.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar consultas para datas passadas.");
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + dto.getPacienteId()));

        ProfissionalSaude profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional de saúde não encontrado com ID: " + dto.getProfissionalId()));

        validarChoqueHorarioMedico(profissional.getId(), dto.getDataHora());

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setDataHora(dto.getDataHora());
        consulta.setMotivoConsulta(dto.getMotivoConsulta());
        consulta.setObservacoesMedicas(dto.getObservacoesMedicas());
        consulta.setStatus(StatusConsulta.AGENDADA);

        Consulta consultaSalva = consultaRepository.save(consulta);
        return new ConsultaDTO(consultaSalva);
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> listarTodas() {
        return consultaRepository.findAll().stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultaDTO buscarPorId(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada com ID: " + id));
        return new ConsultaDTO(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> buscarPorPaciente(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId).stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> buscarPorProfissional(Long profissionalId) {
        return consultaRepository.findByProfissionalId(profissionalId).stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultaDTO cancelarConsulta(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada com ID: " + id));

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraNegocioException("Esta consulta já está cancelada.");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        Consulta consultaAtualizada = consultaRepository.save(consulta);
        return new ConsultaDTO(consultaAtualizada);
    }

    @Transactional
    public ConsultaDTO finalizarConsulta(Long id, String observacoesMedicas) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada com ID: " + id));

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraNegocioException("Não é possível finalizar uma consulta cancelada.");
        }

        consulta.setStatus(StatusConsulta.REALIZADA);
        if (observacoesMedicas != null && !observacoesMedicas.trim().isEmpty()) {
            consulta.setObservacoesMedicas(observacoesMedicas);
        }

        Consulta consultaSalva = consultaRepository.save(consulta);
        return new ConsultaDTO(consultaSalva);
    }

    private void validarChoqueHorarioMedico(Long profissionalId, LocalDateTime dataHora) {
        LocalDateTime inicioJanela = dataHora.minusMinutes(29);
        LocalDateTime fimJanela = dataHora.plusMinutes(29);

        List<Consulta> conflitos = consultaRepository.findConflitoHorarioProfissional(
                profissionalId, inicioJanela, fimJanela, StatusConsulta.CANCELADA);

        if (!conflitos.isEmpty()) {
            throw new ChoqueHorarioException(
                    "O profissional de saúde já possui uma consulta agendada próxima a esse horário (" + dataHora + ")."
            );
        }
    }
}
