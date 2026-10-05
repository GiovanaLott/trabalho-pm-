package com.hospital.dtos;

import com.hospital.entidades.ProfissionalSaude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ProfissionalSaudeDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O registro profissional é obrigatório")
    private String registroProfissional;

    @NotBlank(message = "A especialidade é obrigatória")
    private String especialidade;

    private String telefone;

    @Email(message = "E-mail inválido")
    private String email;

    public ProfissionalSaudeDTO() {
    }

    public ProfissionalSaudeDTO(ProfissionalSaude profissional) {
        if (profissional != null) {
            this.id = profissional.getId();
            this.nome = profissional.getNome();
            this.registroProfissional = profissional.getRegistroProfissional();
            this.especialidade = profissional.getEspecialidade();
            this.telefone = profissional.getTelefone();
            this.email = profissional.getEmail();
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

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public void setRegistroProfissional(String registroProfissional) {
        this.registroProfissional = registroProfissional;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
