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
- Colaborações: `Enfermaria`, `Apartamento`, `UTI`, `QuartoIsolamento`, `Internacao`.

## HistoricoMedico

- Responsabilidades: consolidar consultas, internações e cirurgias de um paciente, inclusive canceladas.
- Colaborações: `Paciente`, `Consulta`, `Internacao`, `ProcedimentoCirurgico`, `HistoricoMedicoService`.

## Serviços REST

- Responsabilidades: aplicar regras, controlar transações, converter entidades/DTOs e expor CRUD sem colocar regra de negócio nos controladores.
- Colaborações: controladores, repositórios, DTOs e entidades correspondentes.
