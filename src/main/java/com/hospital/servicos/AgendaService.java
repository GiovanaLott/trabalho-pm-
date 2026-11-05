package com.hospital.servicos;

import com.hospital.entidades.Consulta;
import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.SituacaoConsulta;
import com.hospital.entidades.StatusCirurgia;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AgendaService {

    private final ConsultaRepository consultaRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public AgendaService(ConsultaRepository consultaRepository,
                         ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.consultaRepository = consultaRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    public void validarDisponibilidade(Long profissionalId,
                                       LocalDateTime inicio,
                                       LocalDateTime fim,
                                       Long consultaIgnoradaId,
                                       Long procedimentoIgnoradoId) {
        if (profissionalId == null || inicio == null || fim == null) {
            throw new RegraNegocioException("Profissional, início e fim são obrigatórios para validar a agenda.");
        }
        if (!fim.isAfter(inicio)) {
            throw new RegraNegocioException("O fim do atendimento deve ser posterior ao início.");
        }

        boolean conflitoConsulta = consultaRepository.findByProfissionalId(profissionalId).stream()
                .filter(consulta -> !Objects.equals(consulta.getId(), consultaIgnoradaId))
                .filter(consulta -> consulta.getSituacao() != SituacaoConsulta.CANCELADA)
                .anyMatch(consulta -> sobrepoe(inicio, fim, consulta.getInicioPrevisto(), consulta.getFimPrevisto()));

        boolean conflitoCirurgia = procedimentoRepository.findByCirurgiaoId(profissionalId).stream()
                .filter(procedimento -> !Objects.equals(procedimento.getId(), procedimentoIgnoradoId))
                .filter(procedimento -> procedimento.getSituacao() != StatusCirurgia.CANCELADA)
                .anyMatch(procedimento -> sobrepoe(inicio, fim,
                        inicioConsiderado(procedimento), fimConsiderado(procedimento)));

        if (conflitoConsulta || conflitoCirurgia) {
            throw new ChoqueHorarioException(
                    "O profissional de saúde já possui um atendimento no intervalo informado.");
        }
    }

    private boolean sobrepoe(LocalDateTime inicioNovo,
                             LocalDateTime fimNovo,
                             LocalDateTime inicioExistente,
                             LocalDateTime fimExistente) {
        return inicioExistente != null && fimExistente != null
                && inicioExistente.isBefore(fimNovo)
                && fimExistente.isAfter(inicioNovo);
    }

    private LocalDateTime inicioConsiderado(ProcedimentoCirurgico procedimento) {
        return procedimento.getInicioEfetivo() != null
                ? procedimento.getInicioEfetivo()
                : procedimento.getInicioPrevisto();
    }

    private LocalDateTime fimConsiderado(ProcedimentoCirurgico procedimento) {
        if (procedimento.getFimEfetivo() != null) {
            return procedimento.getFimEfetivo();
        }
        LocalDateTime inicio = inicioConsiderado(procedimento);
        return inicio != null && procedimento.getDuracaoMinutos() != null
                ? inicio.plusMinutes(procedimento.getDuracaoMinutos())
                : procedimento.getFimPrevisto();
    }
}
