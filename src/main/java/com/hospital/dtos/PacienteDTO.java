package com.hospital.dtos;

import com.hospital.entidades.Paciente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO para transferência de dados de Paciente (entrada e saída da API REST).
 *
 * @author Guilherme Oliveira
 */
public class PacienteDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser uma data no passado")
    private LocalDate dataNascimento;

    private String telefone;
    private String endereco;

    @Email(message = "E-mail inválido")
    private String email;

    public PacienteDTO() {
    }

    public PacienteDTO(Long id, String nome, String cpf, LocalDate dataNascimento, String telefone, String endereco, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
    }

    public PacienteDTO(Paciente paciente) {
        if (paciente != null) {
            this.id = paciente.getId();
            this.nome = paciente.getNome();
            this.cpf = paciente.getCpf();
            this.dataNascimento = paciente.getDataNascimento();
            this.telefone = paciente.getTelefone();
            this.endereco = paciente.getEndereco();
            this.email = paciente.getEmail();
        }
    }

    /**
     * Converte este DTO em uma nova entidade Paciente para persistência.
     *
     * @return Instância de Paciente.
     */
    public Paciente paraEntidade() {
        return new Paciente(this.id, this.nome, this.cpf, this.dataNascimento, this.telefone, this.endereco, this.email);
    }

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
    public String toString() {
        return "PacienteDTO{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", email='" + email + '\'' +
                '}';
    }
}
