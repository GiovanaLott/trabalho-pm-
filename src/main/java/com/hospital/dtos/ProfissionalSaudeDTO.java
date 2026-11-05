package com.hospital.dtos;

import com.hospital.entidades.CategoriaProfissional;
import com.hospital.entidades.Cirurgiao;
import com.hospital.entidades.ClinicoGeral;
import com.hospital.entidades.Especialista;
import com.hospital.entidades.ProfissionalSaude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProfissionalSaudeDTO {

    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O registro profissional é obrigatório")
    private String registroProfissional;

    @NotNull(message = "A categoria profissional é obrigatória")
    private CategoriaProfissional categoria;

    private String especialidade;

    private String telefone;

    @Email(message = "E-mail inválido")
    private String email;

    public ProfissionalSaudeDTO() {
    }

    public ProfissionalSaudeDTO(Long id, String nome, String registroProfissional, String especialidade, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.registroProfissional = registroProfissional;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.email = email;
    }
    public ProfissionalSaudeDTO(ProfissionalSaude profissional) {
        if (profissional != null) {
            this.id = profissional.getId();
            this.nome = profissional.getNome();
            this.registroProfissional = profissional.getRegistroProfissional();
            this.categoria = identificarCategoria(profissional);
            this.especialidade = profissional.getEspecialidade();
            this.telefone = profissional.getTelefone();
            this.email = profissional.getEmail();
        }
    }

    private CategoriaProfissional identificarCategoria(ProfissionalSaude profissional) {
        if (profissional instanceof Especialista) {
            return CategoriaProfissional.ESPECIALISTA;
        }
        if (profissional instanceof Cirurgiao) {
            return CategoriaProfissional.CIRURGIAO;
        }
        if (profissional instanceof ClinicoGeral) {
            return CategoriaProfissional.CLINICO_GERAL;
        }
        return null;
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

    public CategoriaProfissional getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProfissional categoria) {
        this.categoria = categoria;
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
