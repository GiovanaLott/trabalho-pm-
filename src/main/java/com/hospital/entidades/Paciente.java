package com.hospital.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Representa a entidade e modelo de domínio de um Paciente do hospital.
 * Em conformidade com os requisitos mínimos:
 * - Nome
 * - CPF
 * - Data de nascimento
 * - Telefone
 * - Endereço
 * - E-mail para contato
 *
 * @author Guilherme Oliveira
 */
@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 255)
    private String endereco;

    @Column(length = 100)
    private String email;

    /**
     * Construtor padrão (obrigatório para JPA e instanciação sem argumentos).
     */
    public Paciente() {
    }

    /**
     * Construtor completo com ID (para entidades persistidas ou testes).
     */
    public Paciente(Long id, String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
    }

    /**
     * Construtor sem ID (para criação de novas instâncias antes de persistir).
     */
    public Paciente(String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        this(null, nome, cpf, dataNascimento, telefone, endereco, email);
    }

    // --- Métodos de Domínio (Java puro / POO) ---

    /**
     * Calcula a idade atual do paciente em anos com base na data de nascimento.
     *
     * @return Idade em anos, ou 0 se a data de nascimento for nula.
     */
    public int getIdade() {
        if (this.dataNascimento == null) {
            return 0;
        }
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    /**
     * Verifica se o paciente é maior de idade (18 anos ou mais).
     *
     * @return true se maior de idade, false caso contrário.
     */
    public boolean isMaiorDeIdade() {
        return getIdade() >= 18;
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paciente paciente = (Paciente) o;
        return Objects.equals(id, paciente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
