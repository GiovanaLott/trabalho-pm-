# Diagnóstico das Sprints 1 e 2 — estado após implementação

Atualizado em 05/11/2026. Este documento registra o resultado da correção executada a partir do diagnóstico original.

## Resultado

| Requisito | Estado | Evidência principal |
|---|---|---|
| Seis telas da Sprint 1 | Concluído | `static/index.html`, `css/`, `js/app.js`; telas responsivas ligadas à API |
| Hierarquia de profissionais | Concluído | `ProfissionalSaude` abstrata e subclasses `ClinicoGeral`, `Especialista`, `Cirurgiao`; JPA `SINGLE_TABLE` |
| Pesquisa por especialidade | Concluído | `GET /profissionais/especialidade/{especialidade}` restrito a especialistas |
| Base comum de atendimentos | Concluído | `Atendimento` como `@MappedSuperclass`; consultas e cirurgias em tabelas distintas |
| Quatro modalidades e validações | Concluído | rotina, especializada, emergência e cirurgia com validação polimórfica |
| Cirurgia completa | Concluído | tipo, intervalo previsto, início/fim efetivos, observações e cirurgião obrigatório |
| Conflitos de agenda | Concluído | `AgendaService` cruza intervalos previstos e efetivos de consultas e cirurgias; adjacência permitida e cancelados ignorados |
| CRUD de pacientes | Concluído | criar, listar/pesquisar, buscar, atualizar e excluir quando não há histórico |
| CRUD de profissionais | Concluído | criar, listar, buscar, atualizar e excluir quando não há atendimentos |
| CRUD de consultas | Concluído | criar, ler, atualizar e `DELETE` por cancelamento lógico |
| CRUD de internações | Concluído | criar, ler, atualizar, alta e `DELETE` por cancelamento lógico |
| CRUD de quartos | Concluído | criar, filtrar, ler, atualizar, manutenção/liberação e exclusão protegida |
| Histórico médico | Concluído | paciente, consultas, internações e procedimentos cirúrgicos, inclusive registros cancelados |
| Capacidade de quartos | Concluído | validação na entidade e controle de ocupação na admissão/alta/cancelamento |
| Diagrama e CRC | Concluído | `DIAGRAMA_CLASSES.md`, `Diagrama_classes.jpeg`, `crc.md` e `CARTOES_CRC.md` |
| Migração PostgreSQL/Supabase | Preparado | `sql/ajuste_banco_sprint2.sql`, idempotente e sem apagar dados |
| Sprint 3 | Não desenvolvida | nenhuma funcionalidade fora das Sprints 1 e 2 foi adicionada |

## Compatibilidade e preservação

- PostgreSQL/Supabase continua sendo o banco principal; H2 permanece somente para testes e demonstração local.
- O perfil PostgreSQL usa `ddl-auto=validate`; alterações do esquema compartilhado dependem do script versionado e não são executadas automaticamente.
- As colunas legadas `data_hora`, `data_cirurgia`, `horario_inicio_previsto` e `horario_termino` foram preservadas e sincronizadas com os novos campos comuns.
- `StatusConsulta` foi mantido como compatibilidade; `SituacaoConsulta` continua sendo o estado persistido atual.
- Consultas e internações não são removidas fisicamente pela operação REST de exclusão: passam para `CANCELADA`.
- Pacientes, profissionais e quartos só podem ser excluídos quando não possuem histórico dependente.
- O tipo de quarto `ISOLAMENTO` foi preservado por já possuir entidade, persistência e testes.

## Decisões pendentes reais

As dúvidas que dependem do grupo estão em [`duvidas.md`](duvidas.md). A principal pendência externa é classificar os profissionais já existentes no Supabase antes de tornar a coluna discriminadora `categoria` obrigatória. O script SQL não adivinha essa classificação.

## Verificações executadas

- `mvn clean test`: 194 testes em 27 suítes, 0 falhas, 0 erros e 0 ignorados.
- Testes focados da hierarquia, validações, agenda, serviços e controladores.
- Aplicação iniciada com o perfil `h2`, sem acesso ou alteração no Supabase.
- Navegador real em 1440 × 1000 e 390 × 844, sem rolagem horizontal no celular.
- Fluxo manual pelo site: cadastro de paciente, especialista e quarto; agendamento; conflito de horário retornando HTTP 409; internação; prontuário consolidado.
- Inspeção visual automatizada das telas em 1440, 768 e 390 px: nenhuma falha de interface.
