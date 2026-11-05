package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.entidades.Paciente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class PacienteDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Pattern(regexp = "^[0-9()+\\-\\s]{8,20}$", message = "Telefone inválido (use apenas números, espaços, parênteses, + e -)")
    private String telefone;

    @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres")
    private String endereco;

    @Email(message = "E-mail inválido")
    @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres")
    private String email;

    /** Calculada a partir da data de nascimento; ignorada quando enviada pelo cliente. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer idade;

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
            this.idade = paciente.getIdade();
        }
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

    public Integer getIdade() {
        return idade;
    }
}
