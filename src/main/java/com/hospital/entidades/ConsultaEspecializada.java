package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("ESPECIALIZADA")
public class ConsultaEspecializada extends Consulta {

    @Column(name = "especialidade_requerida", length = 80)
    private String especialidade;

    public ConsultaEspecializada() {
        super();
    }

    public ConsultaEspecializada(Long id, Paciente paciente, ProfissionalSaude profissional,
                                 LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas,
                                 SituacaoConsulta situacao, String especialidade) {
        super(id, paciente, profissional, dataHora, motivoConsulta, observacoesMedicas, situacao);
        this.especialidade = especialidade;
    }

    @Override
    public ModalidadeAtendimento getModalidade() {
        return ModalidadeAtendimento.ESPECIALIZADA;
    }

    @Override
    public void validarCondicoesAtendimento() {
        super.validarCondicoesAtendimento();

        if (this.especialidade == null || this.especialidade.trim().isEmpty()) {
            throw new RegraNegocioException("A especialidade médica é obrigatória para consultas especializadas.");
        }

        if (!(getProfissional() instanceof Especialista)) {
            throw new RegraNegocioException(
                    "Uma consulta especializada deve ser realizada por um profissional especialista.");
        }

        if (getProfissional() != null && getProfissional().getEspecialidade() != null) {
            String espProfissional = getProfissional().getEspecialidade().trim();
            if (!espProfissional.equalsIgnoreCase(this.especialidade.trim())) {
                throw new RegraNegocioException(
                        "O profissional selecionado (" + espProfissional +
                        ") não possui especialidade compatível com o atendimento solicitado (" + this.especialidade + ")."
                );
            }
        }
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
}
