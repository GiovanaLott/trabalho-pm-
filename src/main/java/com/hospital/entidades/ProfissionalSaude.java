package com.hospital.entidades;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "profissionais_saude")
public class ProfissionalSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "registro_profissional", nullable = false, unique = true, length = 30)
    private String registroProfissional;

    @Column(nullable = false, length = 80)
    private String especialidade;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    public ProfissionalSaude() {
    }

    public ProfissionalSaude(Long id, String nome, String registroProfissional, String especialidade, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.registroProfissional = registroProfissional;
        this.especialidade = especialidade;
        this.telefone = telefone;
        this.email = email;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfissionalSaude that = (ProfissionalSaude) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
