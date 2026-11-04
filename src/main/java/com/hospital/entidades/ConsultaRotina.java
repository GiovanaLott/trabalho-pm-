package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("ROTINA")
public class ConsultaRotina extends Consulta {

    public ConsultaRotina() {
        super();
    }

    public ConsultaRotina(Long id, Paciente paciente, ProfissionalSaude profissional,
                          LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas,
                          SituacaoConsulta situacao) {
        super(id, paciente, profissional, dataHora, motivoConsulta, observacoesMedicas, situacao);
    }

    @Override
    public ModalidadeAtendimento getModalidade() {
        return ModalidadeAtendimento.ROTINA;
    }

    @Override
    public void validarCondicoesAtendimento() {
        super.validarCondicoesAtendimento();
        if (getMotivoConsulta() == null || getMotivoConsulta().trim().isEmpty()) {
            throw new RegraNegocioException("O motivo do atendimento é obrigatório para consultas de rotina.");
        }
    }
}
