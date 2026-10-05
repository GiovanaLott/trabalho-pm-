package com.hospital.servicos;

import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.entidades.Consulta;
import com.hospital.entidades.HistoricoMedico;
import com.hospital.entidades.Internacao;
import com.hospital.entidades.Paciente;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoricoMedicoService {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final InternacaoRepository internacaoRepository;

    public HistoricoMedicoService(PacienteRepository pacienteRepository,
                                  ConsultaRepository consultaRepository,
                                  InternacaoRepository internacaoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.internacaoRepository = internacaoRepository;
    }

    @Transactional(readOnly = true)
    public HistoricoMedicoDTO buscarHistoricoPorPaciente(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado com ID: " + pacienteId));

        List<Consulta> consultas = consultaRepository.findByPacienteId(pacienteId);
        List<Internacao> internacoes = internacaoRepository.findByPacienteId(pacienteId);

        HistoricoMedico historico = new HistoricoMedico(paciente, consultas, internacoes, "Prontuário consolidado do paciente");
        return new HistoricoMedicoDTO(historico);
    }
}
