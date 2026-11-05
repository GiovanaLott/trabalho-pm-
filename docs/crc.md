# Cartões CRC — Sistema Hospitalar

## Paciente

- Responsabilidades: manter dados cadastrais, CPF único e idade calculada.
- Colaborações: `Consulta`, `Internacao`, `ProcedimentoCirurgico`, `HistoricoMedico`.

## ProfissionalSaude (abstrata)

- Responsabilidades: concentrar nome, registro, contato e qualificação comum; fornecer identidade persistente da equipe.
- Colaborações: `ClinicoGeral`, `Especialista`, `Cirurgiao`, `Atendimento`, `Internacao`.

## ClinicoGeral

- Responsabilidades: representar o profissional de clínica geral.
- Colaborações: `ProfissionalSaude`, `ConsultaRotina`, `AtendimentoEmergencia`.

## Especialista

- Responsabilidades: declarar uma especialidade obrigatória; atender somente consulta especializada compatível; permitir pesquisa por área.
- Colaborações: `ProfissionalSaude`, `ConsultaEspecializada`, `ProfissionalSaudeRepository`.

## Cirurgiao

- Responsabilidades: representar o profissional autorizado a realizar procedimentos cirúrgicos.
- Colaborações: `ProfissionalSaude`, `ProcedimentoCirurgico`.

## Atendimento (abstrata, MappedSuperclass)

- Responsabilidades: compartilhar paciente, profissional, início previsto, duração positiva, fim calculado e validações comuns.
- Colaborações: `Paciente`, `ProfissionalSaude`, `Consulta`, `ProcedimentoCirurgico`.

## Consulta (abstrata)

- Responsabilidades: manter motivo, observações e situação; validar transições; oferecer confirmação, início, conclusão, ausência e cancelamento lógico.
- Colaborações: `Atendimento`, `ConsultaRotina`, `ConsultaEspecializada`, `AtendimentoEmergencia`, `AgendaService`.

## ConsultaRotina

- Responsabilidades: representar consulta regular e exigir motivo.
- Colaborações: `Consulta`, `ClinicoGeral`.

## ConsultaEspecializada

- Responsabilidades: exigir especialidade e um `Especialista` de área compatível.
- Colaborações: `Consulta`, `Especialista`.

## AtendimentoEmergencia

- Responsabilidades: classificar prioridade e acompanhar o fluxo de atendimento emergencial.
- Colaborações: `Consulta`, `PrioridadeAtendimento`.

## ProcedimentoCirurgico

- Responsabilidades: registrar tipo, descrição, previsão, duração, horários efetivos, observações pré/pós, situação; exigir cirurgião; impedir conclusão antes do início.
- Colaborações: `Atendimento`, `Cirurgiao`, `StatusCirurgia`, `AgendaService`.

## AgendaService

- Responsabilidades: centralizar a verificação de intervalos `[início, fim)`; cruzar consultas e cirurgias; ignorar cancelados e o próprio registro em atualização.
- Colaborações: `ConsultaRepository`, `ProcedimentoCirurgicoRepository`, `ConsultaService`, `ProcedimentoCirurgicoService`.

## Internacao

- Responsabilidades: registrar entrada, previsão/efetivação da alta, observações e situação; preservar registros cancelados.
- Colaborações: `Paciente`, `ProfissionalSaude`, `Quarto`, `InternacaoService`.

## Quarto (abstrata)

- Responsabilidades: controlar capacidade e ocupação; impedir lotação; validar manutenção e liberar leito na alta/cancelamento.
- Colaborações: `Enfermaria`, `Apartamento`, `UnidadeTerapiaIntensiva`, `QuartoIsolamento`, `Internacao`.

## Enfermaria

- Responsabilidades: representar quarto coletivo e limitar a capacidade entre dois e oito leitos.
- Colaborações: `Quarto`, `Internacao`.

## Apartamento

- Responsabilidades: representar quarto privativo, controlar até dois leitos e informar se aceita acompanhante.
- Colaborações: `Quarto`, `Internacao`.

## UnidadeTerapiaIntensiva

- Responsabilidades: representar leito de UTI e exigir justificativa clínica para a admissão.
- Colaborações: `Quarto`, `Internacao`.

## QuartoIsolamento

- Responsabilidades: manter um único leito e exigir a classificação do isolamento.
- Colaborações: `Quarto`, `TipoIsolamento`, `Internacao`.

## Cpf

- Responsabilidades: normalizar, validar e formatar o CPF do paciente como objeto de valor.
- Colaborações: `Paciente`, `PacienteService`.

## CategoriaProfissional

- Responsabilidades: restringir o discriminador profissional a clínico geral, especialista ou cirurgião.
- Colaborações: `ProfissionalSaude`, `ProfissionalSaudeDTO`.

## ModalidadeAtendimento

- Responsabilidades: identificar consulta de rotina, especializada ou emergência.
- Colaborações: `Consulta`, `ConsultaRequestDTO`.

## PrioridadeAtendimento

- Responsabilidades: classificar a prioridade clínica dos atendimentos de emergência.
- Colaborações: `AtendimentoEmergencia`.

## SituacaoConsulta e StatusConsulta

- Responsabilidades: controlar o fluxo atual da consulta e preservar a enumeração legada usada por integrações antigas.
- Colaborações: `Consulta`, `ConsultaService`.

## StatusCirurgia

- Responsabilidades: controlar as transições de cirurgia agendada, em andamento, concluída ou cancelada.
- Colaborações: `ProcedimentoCirurgico`, `ProcedimentoCirurgicoService`.

## StatusInternacao

- Responsabilidades: distinguir internação em andamento, alta e cancelamento lógico.
- Colaborações: `Internacao`, `InternacaoService`.

## StatusQuarto e TipoQuarto

- Responsabilidades: controlar disponibilidade/manutenção e selecionar a subclasse de quarto.
- Colaborações: `Quarto`, `QuartoService`.

## TipoIsolamento

- Responsabilidades: classificar isolamento de contato, gotículas ou aerossóis.
- Colaborações: `QuartoIsolamento`.

## HistoricoMedico

- Responsabilidades: consolidar consultas, internações e cirurgias de um paciente, inclusive canceladas.
- Colaborações: `Paciente`, `Consulta`, `Internacao`, `ProcedimentoCirurgico`, `HistoricoMedicoService`.

## Serviços REST

- Responsabilidades: aplicar regras, controlar transações, converter entidades/DTOs e expor CRUD sem colocar regra de negócio nos controladores.
- Colaborações: controladores, repositórios, DTOs e entidades correspondentes.
