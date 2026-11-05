package com.hospital.servicos;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.entidades.*;
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

        ModalidadeAtendimento modalidade = dto.getModalidade() != null ? dto.getModalidade() : ModalidadeAtendimento.ROTINA;

        // Atendimentos de emergência podem ser registrados no momento atual
        if (modalidade != ModalidadeAtendimento.EMERGENCIA && dto.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar consultas para datas passadas.");
        }

        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + dto.getPacienteId()));

        ProfissionalSaude profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional de saúde não encontrado com ID: " + dto.getProfissionalId()));

        validarChoqueHorarioMedico(profissional.getId(), dto.getDataHora());

        Consulta consulta = criarInstanciaPorModalidade(modalidade, dto);
        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setDataHora(dto.getDataHora());
        consulta.setMotivoConsulta(dto.getMotivoConsulta());
        consulta.setObservacoesMedicas(dto.getObservacoesMedicas());
        consulta.setSituacao(SituacaoConsulta.AGENDADA);

        // Validação polimórfica específica de cada modalidade
        consulta.validarCondicoesAtendimento();

        Consulta consultaSalva = consultaRepository.save(consulta);
        return new ConsultaDTO(consultaSalva);
    }

    private Consulta criarInstanciaPorModalidade(ModalidadeAtendimento modalidade, ConsultaRequestDTO dto) {
        return switch (modalidade) {
            case ESPECIALIZADA -> {
                ConsultaEspecializada esp = new ConsultaEspecializada();
                esp.setEspecialidade(dto.getEspecialidade());
                yield esp;
            }
            case EMERGENCIA -> {
                AtendimentoEmergencia emergencia = new AtendimentoEmergencia();
                emergencia.setPrioridade(dto.getPrioridade());
                yield emergencia;
            }
            case ROTINA -> new ConsultaRotina();
        };
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> listarTodas() {
        return consultaRepository.findAll().stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultaDTO buscarPorId(Long id) {
        Consulta consulta = buscarEntidadePorId(id);
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

    @Transactional(readOnly = true)
    public List<ConsultaDTO> buscarPorSituacao(SituacaoConsulta situacao) {
        return consultaRepository.findBySituacao(situacao).stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> buscarPorModalidade(ModalidadeAtendimento modalidade) {
        Class<? extends Consulta> tipo = switch (modalidade) {
            case ROTINA -> ConsultaRotina.class;
            case ESPECIALIZADA -> ConsultaEspecializada.class;
            case EMERGENCIA -> AtendimentoEmergencia.class;
        };

        return consultaRepository.findByTipoConsulta(tipo).stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> buscarEmergenciasAguardando() {
        return consultaRepository.findEmergenciasAguardandoAvaliacao().stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultaDTO confirmarConsulta(Long id) {
        Consulta consulta = buscarEntidadePorId(id);
        consulta.confirmar();
        return new ConsultaDTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO iniciarAtendimento(Long id) {
        Consulta consulta = buscarEntidadePorId(id);
        consulta.iniciarAtendimento();
        return new ConsultaDTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO finalizarConsulta(Long id, String observacoesMedicas) {
        Consulta consulta = buscarEntidadePorId(id);
        consulta.concluir(observacoesMedicas);
        return new ConsultaDTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO cancelarConsulta(Long id) {
        Consulta consulta = buscarEntidadePorId(id);
        consulta.cancelar();
        return new ConsultaDTO(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaDTO registrarNaoComparecimento(Long id) {
        Consulta consulta = buscarEntidadePorId(id);
        consulta.registrarNaoComparecimento();
        return new ConsultaDTO(consultaRepository.save(consulta));
    }

    private Consulta buscarEntidadePorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada com ID: " + id));
    }

    private void validarChoqueHorarioMedico(Long profissionalId, LocalDateTime dataHora) {
        LocalDateTime inicioJanela = dataHora.minusMinutes(29);
        LocalDateTime fimJanela = dataHora.plusMinutes(29);

        List<Consulta> conflitos = consultaRepository.findConflitoHorarioProfissional(
                profissionalId, inicioJanela, fimJanela);

        if (!conflitos.isEmpty()) {
            throw new ChoqueHorarioException(
                    "O profissional de saúde já possui uma consulta agendada próxima a esse horário (" + dataHora + ")."
            );
        }
    }
}
