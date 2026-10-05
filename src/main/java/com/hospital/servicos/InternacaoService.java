package com.hospital.servicos;

import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import com.hospital.repositorios.QuartoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternacaoService {

    private final InternacaoRepository internacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final QuartoRepository quartoRepository;

    public InternacaoService(InternacaoRepository internacaoRepository,
                             PacienteRepository pacienteRepository,
                             ProfissionalSaudeRepository profissionalRepository,
                             QuartoRepository quartoRepository) {
        this.internacaoRepository = internacaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.quartoRepository = quartoRepository;
    }

    @Transactional
    public InternacaoDTO realizarInternacao(InternacaoRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + dto.getPacienteId()));

        ProfissionalSaude profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional de saúde não encontrado com ID: " + dto.getProfissionalId()));

        Quarto quarto = quartoRepository.findById(dto.getQuartoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado com ID: " + dto.getQuartoId()));

        if (internacaoRepository.existsByPacienteIdAndStatus(paciente.getId(), StatusInternacao.EM_ANDAMENTO)) {
            throw new RegraNegocioException("O paciente " + paciente.getNome() + " já possui uma internação em andamento.");
        }

        if (quarto.getOcupacaoAtual() >= quarto.getCapacidadeMaxima()) {
            throw new QuartoLotadoException("O quarto " + quarto.getNumeroIdentificacao() + " atingiu sua capacidade máxima (" + quarto.getCapacidadeMaxima() + " leitos).");
        }

        quarto.incrementarOcupacao();
        quartoRepository.save(quarto);

        Internacao internacao = new Internacao();
        internacao.setPaciente(paciente);
        internacao.setProfissional(profissional);
        internacao.setQuarto(quarto);
        internacao.setDataEntrada(dto.getDataEntrada() != null ? dto.getDataEntrada() : LocalDateTime.now());
        internacao.setDataPrevistaAlta(dto.getDataPrevistaAlta());
        internacao.setObservacoes(dto.getObservacoes());
        internacao.setStatus(StatusInternacao.EM_ANDAMENTO);

        Internacao salva = internacaoRepository.save(internacao);
        return new InternacaoDTO(salva);
    }

    @Transactional
    public InternacaoDTO darAlta(Long internacaoId) {
        Internacao internacao = internacaoRepository.findById(internacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Internação não encontrada com ID: " + internacaoId));

        if (internacao.getStatus() != StatusInternacao.EM_ANDAMENTO) {
            throw new RegraNegocioException("Apenas internações em andamento podem receber alta.");
        }

        internacao.setStatus(StatusInternacao.ALTA);
        internacao.setDataEfetivaAlta(LocalDateTime.now());

        Quarto quarto = internacao.getQuarto();
        quarto.decrementarOcupacao();
        quartoRepository.save(quarto);

        Internacao atualizada = internacaoRepository.save(internacao);
        return new InternacaoDTO(atualizada);
    }

    @Transactional(readOnly = true)
    public List<InternacaoDTO> listarTodas() {
        return internacaoRepository.findAll().stream()
                .map(InternacaoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InternacaoDTO> listarEmAndamento() {
        return internacaoRepository.findByStatus(StatusInternacao.EM_ANDAMENTO).stream()
                .map(InternacaoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InternacaoDTO buscarPorId(Long id) {
        Internacao internacao = internacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Internação não encontrada com ID: " + id));
        return new InternacaoDTO(internacao);
    }

    @Transactional(readOnly = true)
    public List<InternacaoDTO> buscarPorPaciente(Long pacienteId) {
        return internacaoRepository.findByPacienteId(pacienteId).stream()
                .map(InternacaoDTO::new)
                .collect(Collectors.toList());
    }
}
