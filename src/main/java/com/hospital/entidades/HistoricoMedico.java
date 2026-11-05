package com.hospital.entidades;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de domínio puro para representar o prontuário / histórico médico
 * consolidado de um paciente (consultas e internações).
 */
public class HistoricoMedico {

    private Paciente paciente;
    private List<Consulta> consultas = new ArrayList<>();
    private List<Internacao> internacoes = new ArrayList<>();
    private List<ProcedimentoCirurgico> procedimentosCirurgicos = new ArrayList<>();

    public HistoricoMedico() {
    }

    public HistoricoMedico(Paciente paciente, List<Consulta> consultas, List<Internacao> internacoes) {
        this.paciente = paciente;
        this.consultas = consultas != null ? consultas : new ArrayList<>();
        this.internacoes = internacoes != null ? internacoes : new ArrayList<>();
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public List<Consulta> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = consultas;
    }

    public List<Internacao> getInternacoes() {
        return internacoes;
    }

    public void setInternacoes(List<Internacao> internacoes) {
        this.internacoes = internacoes;
    }

    public void adicionarConsulta(Consulta consulta) {
        this.consultas.add(consulta);
    }

    public void adicionarInternacao(Internacao internacao) {
        this.internacoes.add(internacao);
    }

    public List<ProcedimentoCirurgico> getProcedimentosCirurgicos() {
        return procedimentosCirurgicos;
    }

    public void setProcedimentosCirurgicos(List<ProcedimentoCirurgico> procedimentosCirurgicos) {
        this.procedimentosCirurgicos = procedimentosCirurgicos != null
                ? procedimentosCirurgicos : new ArrayList<>();
    }

    public void adicionarProcedimentoCirurgico(ProcedimentoCirurgico procedimento) {
        this.procedimentosCirurgicos.add(procedimento);
    }
}
