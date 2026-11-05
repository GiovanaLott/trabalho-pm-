package com.hospital.servicos;

import com.hospital.dtos.ConclusaoCirurgiaDTO;
import com.hospital.dtos.ProcedimentoCirurgicoDTO;
import com.hospital.dtos.ProcedimentoCirurgicoRequestDTO;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.Cirurgiao;
import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.StatusCirurgia;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcedimentoCirurgicoService {

    private final ProcedimentoCirurgicoRepository procedimentoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final AgendaService agendaService;

    public ProcedimentoCirurgicoService(ProcedimentoCirurgicoRepository procedimentoRepository,
                                       PacienteRepository pacienteRepository,
                                       ProfissionalSaudeRepository profissionalRepository,
                                       AgendaService agendaService) {
        this.procedimentoRepository = procedimentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.agendaService = agendaService;
    }

    @Transactional
    public ProcedimentoCirurgicoDTO agendar(ProcedimentoCirurgicoRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + dto.getPacienteId()));

        ProfissionalSaude cirurgiao = profissionalRepository.findById(dto.getCirurgiaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cirurgião não encontrado com ID: " + dto.getCirurgiaoId()));

        if (!(cirurgiao instanceof Cirurgiao)) {
            throw new RegraNegocioException(
                    "O profissional informado deve pertencer à categoria cirurgião.");
        }

        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico();
        procedimento.setDescricao(dto.getDescricao());
        procedimento.setTipo(dto.getTipo());
        procedimento.setPaciente(paciente);
        procedimento.setCirurgiao(cirurgiao);
        procedimento.setData(dto.getData());
        procedimento.setHorarioInicioPrevisto(dto.getHorarioInicioPrevisto());
        procedimento.setDuracaoMinutos(calcularDuracaoPrevista(dto));
        procedimento.setObservacoesPreOperatorias(dto.getObservacoesPreOperatorias());
        procedimento.setSituacao(StatusCirurgia.AGENDADA);
        procedimento.validarCondicoesAtendimento();
        agendaService.validarDisponibilidade(cirurgiao.getId(), procedimento.getInicioPrevisto(),
                procedimento.getFimPrevisto(), null, null);

        ProcedimentoCirurgico salvo = procedimentoRepository.save(procedimento);
        return new ProcedimentoCirurgicoDTO(salvo);
    }

    @Transactional
    public ProcedimentoCirurgicoDTO iniciarCirurgia(Long id) {
        ProcedimentoCirurgico procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico não encontrado com ID: " + id));

        if (procedimento.getSituacao() == StatusCirurgia.CANCELADA) {
            throw new RegraNegocioException("Não é possível iniciar uma cirurgia cancelada.");
        }

        if (procedimento.getSituacao() == StatusCirurgia.CONCLUIDA) {
            throw new RegraNegocioException("Não é possível iniciar uma cirurgia que já foi concluída.");
        }

        if (procedimento.getSituacao() != StatusCirurgia.AGENDADA) {
            throw new RegraNegocioException("Apenas cirurgias agendadas podem ser iniciadas.");
        }

        LocalDateTime inicioEfetivo = LocalDateTime.now();
        LocalDateTime fimEstimado = inicioEfetivo.plusMinutes(procedimento.getDuracaoMinutos());
        agendaService.validarDisponibilidade(procedimento.getCirurgiao().getId(), inicioEfetivo,
                fimEstimado, null, procedimento.getId());
        procedimento.setSituacao(StatusCirurgia.EM_ANDAMENTO);
        procedimento.setInicioEfetivo(inicioEfetivo);
        ProcedimentoCirurgico atualizado = procedimentoRepository.save(procedimento);
        return new ProcedimentoCirurgicoDTO(atualizado);
    }

    @Transactional
    public ProcedimentoCirurgicoDTO concluirCirurgia(Long id, ConclusaoCirurgiaDTO dto) {
        ProcedimentoCirurgico procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico não encontrado com ID: " + id));

        if (procedimento.getSituacao() == StatusCirurgia.AGENDADA) {
            throw new RegraNegocioException("Uma cirurgia não pode ser concluída se ainda não tiver sido iniciada.");
        }

        if (procedimento.getSituacao() == StatusCirurgia.CANCELADA) {
            throw new RegraNegocioException("Não é possível concluir uma cirurgia cancelada.");
        }

        if (procedimento.getSituacao() == StatusCirurgia.CONCLUIDA) {
            throw new RegraNegocioException("Esta cirurgia já foi concluída anteriormente.");
        }

        if (procedimento.getSituacao() != StatusCirurgia.EM_ANDAMENTO) {
            throw new RegraNegocioException("Apenas cirurgias em andamento podem ser concluídas.");
        }

        LocalDateTime fimEfetivo = resolverFimEfetivo(procedimento, dto);
        if (procedimento.getInicioEfetivo() != null
                && fimEfetivo.isBefore(procedimento.getInicioEfetivo())) {
            throw new RegraNegocioException(
                    "O horário efetivo de término não pode ser anterior ao início da cirurgia.");
        }
        procedimento.setFimEfetivo(fimEfetivo);
        if (dto != null && dto.getObservacoesPosOperatorias() != null) {
            procedimento.setObservacoesPosOperatorias(dto.getObservacoesPosOperatorias());
        }

        procedimento.setSituacao(StatusCirurgia.CONCLUIDA);
        ProcedimentoCirurgico atualizado = procedimentoRepository.save(procedimento);
        return new ProcedimentoCirurgicoDTO(atualizado);
    }

    @Transactional
    public ProcedimentoCirurgicoDTO cancelarCirurgia(Long id) {
        ProcedimentoCirurgico procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico não encontrado com ID: " + id));

        if (procedimento.getSituacao() == StatusCirurgia.CONCLUIDA) {
            throw new RegraNegocioException("Não é possível cancelar uma cirurgia que já foi concluída.");
        }

        if (procedimento.getSituacao() == StatusCirurgia.CANCELADA) {
            throw new RegraNegocioException("Esta cirurgia já está cancelada.");
        }

        procedimento.setSituacao(StatusCirurgia.CANCELADA);
        ProcedimentoCirurgico atualizado = procedimentoRepository.save(procedimento);
        return new ProcedimentoCirurgicoDTO(atualizado);
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgicoDTO> listarTodos() {
        return procedimentoRepository.findAll().stream()
                .map(ProcedimentoCirurgicoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcedimentoCirurgicoDTO buscarPorId(Long id) {
        ProcedimentoCirurgico procedimento = procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico não encontrado com ID: " + id));
        return new ProcedimentoCirurgicoDTO(procedimento);
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgicoDTO> buscarPorPaciente(Long pacienteId) {
        return procedimentoRepository.findByPacienteId(pacienteId).stream()
                .map(ProcedimentoCirurgicoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgicoDTO> buscarPorCirurgiao(Long cirurgiaoId) {
        return procedimentoRepository.findByCirurgiaoId(cirurgiaoId).stream()
                .map(ProcedimentoCirurgicoDTO::new)
                .collect(Collectors.toList());
    }

    private int calcularDuracaoPrevista(ProcedimentoCirurgicoRequestDTO dto) {
        if (dto.getHorarioFimPrevisto() == null) {
            return 60;
        }
        long minutos = Duration.between(dto.getHorarioInicioPrevisto(), dto.getHorarioFimPrevisto()).toMinutes();
        if (minutos <= 0) {
            throw new RegraNegocioException(
                    "O horário previsto de término deve ser posterior ao início da cirurgia.");
        }
        return Math.toIntExact(minutos);
    }

    private LocalDateTime resolverFimEfetivo(ProcedimentoCirurgico procedimento,
                                              ConclusaoCirurgiaDTO dto) {
        if (dto != null && dto.getFimEfetivo() != null) {
            return dto.getFimEfetivo();
        }
        if (dto != null && dto.getHorarioTermino() != null) {
            LocalDateTime referencia = procedimento.getInicioEfetivo() != null
                    ? procedimento.getInicioEfetivo()
                    : procedimento.getInicioPrevisto();
            return LocalDateTime.of(referencia.toLocalDate(), dto.getHorarioTermino());
        }
        return LocalDateTime.now();
    }
}
