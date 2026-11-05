package com.hospital.servicos;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.ProcedimentoCirurgicoDTO;
import com.hospital.entidades.Paciente;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoMedicoService {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public HistoricoMedicoService(PacienteRepository pacienteRepository,
                                  ConsultaRepository consultaRepository,
                                  InternacaoRepository internacaoRepository,
                                  ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.internacaoRepository = internacaoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional(readOnly = true)
    public HistoricoMedicoDTO buscarHistoricoPorPaciente(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + pacienteId));

        List<ConsultaDTO> consultas = consultaRepository.findByPacienteId(pacienteId).stream()
                .map(ConsultaDTO::new)
                .collect(Collectors.toList());

        List<InternacaoDTO> internacoes = internacaoRepository.findByPacienteId(pacienteId).stream()
                .map(InternacaoDTO::new)
                .collect(Collectors.toList());

        List<ProcedimentoCirurgicoDTO> procedimentos = procedimentoRepository.findByPacienteId(pacienteId).stream()
                .map(ProcedimentoCirurgicoDTO::new)
                .collect(Collectors.toList());

        return new HistoricoMedicoDTO(new PacienteDTO(paciente), consultas, internacoes, procedimentos);
    }
}
