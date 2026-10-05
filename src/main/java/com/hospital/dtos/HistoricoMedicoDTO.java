package com.hospital.dtos;

import java.util.ArrayList;
import java.util.List;

public class HistoricoMedicoDTO {

    private PacienteDTO paciente;
    private List<ConsultaDTO> consultas = new ArrayList<>();
    private List<InternacaoDTO> internacoes = new ArrayList<>();

    public HistoricoMedicoDTO() {
    }

    public HistoricoMedicoDTO(PacienteDTO paciente, List<ConsultaDTO> consultas, List<InternacaoDTO> internacoes) {
        this.paciente = paciente;
        this.consultas = consultas != null ? consultas : new ArrayList<>();
        this.internacoes = internacoes != null ? internacoes : new ArrayList<>();
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
    }

    public List<InternacaoDTO> getInternacoes() {
        return internacoes;
    }

    public void setInternacoes(List<InternacaoDTO> internacoes) {
        this.internacoes = internacoes;
    }
}
