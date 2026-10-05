package com.hospital.entidades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa o modelo de domínio em Java puro de um Histórico Médico (Prontuário) de um paciente.
 * Em conformidade com o requisito do sistema:
 * "O sistema deverá permitir consultar o histórico de um paciente contendo:
 *  - Consultas realizadas;
 *  - Internações realizadas;
 *  - Informações relevantes registradas durante os atendimentos."
 *
 * Esta classe encapsula as regras de consolidação dos atendimentos de um paciente.
 *
 * @author Guilherme Oliveira
 */
public class HistoricoMedico {

    private Paciente paciente;
    private List<Consulta> consultas = new ArrayList<>();
    private List<Internacao> internacoes = new ArrayList<>();
    private String informacoesRelevantes;

    /**
     * Construtor padrão sem argumentos.
     */
    public HistoricoMedico() {
    }

    /**
     * Construtor com o paciente associado.
     */
    public HistoricoMedico(Paciente paciente) {
        this.paciente = paciente;
    }

    /**
     * Construtor completo com paciente, consultas, internações e anotações relevantes.
     */
    public HistoricoMedico(Paciente paciente, List<Consulta> consultas, List<Internacao> internacoes, String informacoesRelevantes) {
        this.paciente = paciente;
        this.consultas = (consultas != null) ? new ArrayList<>(consultas) : new ArrayList<>();
        this.internacoes = (internacoes != null) ? new ArrayList<>(internacoes) : new ArrayList<>();
        this.informacoesRelevantes = informacoesRelevantes;
    }

    // --- Métodos de Domínio (Java puro / POO) ---

    /**
     * Adiciona uma consulta ao histórico do paciente.
     *
     * @param consulta Instância de consulta a ser adicionada.
     */
    public void adicionarConsulta(Consulta consulta) {
        if (consulta != null) {
            this.consultas.add(consulta);
        }
    }

    /**
     * Adiciona uma internação ao histórico do paciente.
     *
     * @param internacao Instância de internação a ser adicionada.
     */
    public void adicionarInternacao(Internacao internacao) {
        if (internacao != null) {
            this.internacoes.add(internacao);
        }
    }

    /**
     * Retorna a quantidade total de consultas registradas no histórico.
     */
    public int getTotalConsultas() {
        return this.consultas.size();
    }

    /**
     * Retorna a quantidade total de internações registradas no histórico.
     */
    public int getTotalInternacoes() {
        return this.internacoes.size();
    }

    /**
     * Verifica se o paciente possui alguma internação em andamento no momento.
     *
     * @return true se houver internação ativa, false caso contrário.
     */
    public boolean possuiInternacaoAtiva() {
        return this.internacoes.stream().anyMatch(Internacao::isEmAndamento);
    }

    /**
     * Obtém a internação atualmente ativa, caso exista.
     */
    public Optional<Internacao> getInternacaoAtiva() {
        return this.internacoes.stream()
                .filter(Internacao::isEmAndamento)
                .findFirst();
    }

    /**
     * Filtra e retorna apenas as consultas cujo status é REALIZADA.
     */
    public List<Consulta> getConsultasRealizadas() {
        return this.consultas.stream()
                .filter(Consulta::isRealizada)
                .toList();
    }

    /**
     * Filtra e retorna apenas as internações com alta concedida.
     */
    public List<Internacao> getInternacoesComAlta() {
        return this.internacoes.stream()
                .filter(Internacao::isAlta)
                .toList();
    }

    // --- Getters e Setters ---

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public List<Consulta> getConsultas() {
        return Collections.unmodifiableList(consultas);
    }

    public void setConsultas(List<Consulta> consultas) {
        this.consultas = (consultas != null) ? new ArrayList<>(consultas) : new ArrayList<>();
    }

    public List<Internacao> getInternacoes() {
        return Collections.unmodifiableList(internacoes);
    }

    public void setInternacoes(List<Internacao> internacoes) {
        this.internacoes = (internacoes != null) ? new ArrayList<>(internacoes) : new ArrayList<>();
    }

    public String getInformacoesRelevantes() {
        return informacoesRelevantes;
    }

    public void setInformacoesRelevantes(String informacoesRelevantes) {
        this.informacoesRelevantes = informacoesRelevantes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoMedico that = (HistoricoMedico) o;
        return Objects.equals(paciente, that.paciente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paciente);
    }

    @Override
    public String toString() {
        return "HistoricoMedico{" +
                "paciente=" + (paciente != null ? paciente.getNome() : null) +
                ", totalConsultas=" + getTotalConsultas() +
                ", totalInternacoes=" + getTotalInternacoes() +
                ", internacaoAtiva=" + possuiInternacaoAtiva() +
                '}';
    }
}
