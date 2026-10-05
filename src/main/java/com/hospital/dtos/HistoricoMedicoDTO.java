package com.hospital.dtos;

import com.hospital.entidades.HistoricoMedico;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para transferência e saída dos dados do Histórico Médico / Prontuário do Paciente.
 *
 * @author Guilherme Oliveira
 */
public class HistoricoMedicoDTO {

    private PacienteDTO paciente;
    private List<ConsultaDTO> consultas = new ArrayList<>();
    private List<InternacaoDTO> internacoes = new ArrayList<>();
    private String informacoesRelevantes;
    private int totalConsultas;
    private int totalInternacoes;
    private boolean possuiInternacaoAtiva;

    public HistoricoMedicoDTO() {
    }

    public HistoricoMedicoDTO(PacienteDTO paciente, List<ConsultaDTO> consultas, List<InternacaoDTO> internacoes) {
        this(paciente, consultas, internacoes, null);
    }

    public HistoricoMedicoDTO(PacienteDTO paciente, List<ConsultaDTO> consultas, List<InternacaoDTO> internacoes, String informacoesRelevantes) {
        this.paciente = paciente;
        this.consultas = consultas != null ? consultas : new ArrayList<>();
        this.internacoes = internacoes != null ? internacoes : new ArrayList<>();
        this.informacoesRelevantes = informacoesRelevantes;
        this.totalConsultas = this.consultas.size();
        this.totalInternacoes = this.internacoes.size();
        this.possuiInternacaoAtiva = this.internacoes.stream()
                .anyMatch(i -> i.getStatus() != null && "EM_ANDAMENTO".equalsIgnoreCase(i.getStatus().name()));
    }

    /**
     * Construtor que recebe a classe de modelo HistoricoMedico em Java puro.
     *
     * @param historico Instância do modelo de domínio HistoricoMedico.
     */
    public HistoricoMedicoDTO(HistoricoMedico historico) {
        if (historico != null) {
            this.paciente = historico.getPaciente() != null ? new PacienteDTO(historico.getPaciente()) : null;
            this.consultas = historico.getConsultas() != null ?
                    historico.getConsultas().stream().map(ConsultaDTO::new).collect(Collectors.toList()) : new ArrayList<>();
            this.internacoes = historico.getInternacoes() != null ?
                    historico.getInternacoes().stream().map(InternacaoDTO::new).collect(Collectors.toList()) : new ArrayList<>();
            this.informacoesRelevantes = historico.getInformacoesRelevantes();
            this.totalConsultas = historico.getTotalConsultas();
            this.totalInternacoes = historico.getTotalInternacoes();
            this.possuiInternacaoAtiva = historico.possuiInternacaoAtiva();
        }
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    public List<ConsultaDTO> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<ConsultaDTO> consultas) {
        this.consultas = consultas;
        this.totalConsultas = (consultas != null) ? consultas.size() : 0;
    }

    public List<InternacaoDTO> getInternacoes() {
        return internacoes;
    }

    public void setInternacoes(List<InternacaoDTO> internacoes) {
        this.internacoes = internacoes;
        this.totalInternacoes = (internacoes != null) ? internacoes.size() : 0;
    }

    public String getInformacoesRelevantes() {
        return informacoesRelevantes;
    }

    public void setInformacoesRelevantes(String informacoesRelevantes) {
        this.informacoesRelevantes = informacoesRelevantes;
    }

    public int getTotalConsultas() {
        return totalConsultas;
    }

    public void setTotalConsultas(int totalConsultas) {
        this.totalConsultas = totalConsultas;
    }

    public int getTotalInternacoes() {
        return totalInternacoes;
    }

    public void setTotalInternacoes(int totalInternacoes) {
        this.totalInternacoes = totalInternacoes;
    }

    public boolean isPossuiInternacaoAtiva() {
        return possuiInternacaoAtiva;
    }

    public void setPossuiInternacaoAtiva(boolean possuiInternacaoAtiva) {
        this.possuiInternacaoAtiva = possuiInternacaoAtiva;
    }

    @Override
    public String toString() {
        return "HistoricoMedicoDTO{" +
                "paciente=" + (paciente != null ? paciente.getNome() : null) +
                ", totalConsultas=" + totalConsultas +
                ", totalInternacoes=" + totalInternacoes +
                ", possuiInternacaoAtiva=" + possuiInternacaoAtiva +
                '}';
    }
}
