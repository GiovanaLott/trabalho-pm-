# Diagrama de classes — Sprints 1 e 2

O modelo usa herança `SINGLE_TABLE` para profissionais e consultas. `Atendimento` é uma `@MappedSuperclass`, portanto consultas e cirurgias continuam em tabelas separadas.

```mermaid
classDiagram
direction LR

class Paciente {
  +Long id
  +String nome
  +String cpf
  +LocalDate dataNascimento
}

class ProfissionalSaude {
  <<abstract>>
  +Long id
  +String nome
  +String registroProfissional
  +String especialidade
}
class ClinicoGeral
class Especialista
class Cirurgiao
ProfissionalSaude <|-- ClinicoGeral
ProfissionalSaude <|-- Especialista
ProfissionalSaude <|-- Cirurgiao

class Atendimento {
  <<MappedSuperclass>>
  +Long id
  +LocalDateTime inicioPrevisto
  +Integer duracaoMinutos
  +getFimPrevisto()
  +validarCondicoesAtendimento()
}
class Consulta {
  <<abstract>>
  +String motivoConsulta
  +SituacaoConsulta situacao
}
class ConsultaRotina
class ConsultaEspecializada {
  +String especialidade
}
class AtendimentoEmergencia {
  +PrioridadeAtendimento prioridade
}
class ProcedimentoCirurgico {
  +String tipo
  +String descricao
  +LocalDateTime inicioEfetivo
  +LocalDateTime fimEfetivo
  +StatusCirurgia situacao
}
Atendimento <|-- Consulta
Atendimento <|-- ProcedimentoCirurgico
Consulta <|-- ConsultaRotina
Consulta <|-- ConsultaEspecializada
Consulta <|-- AtendimentoEmergencia

class Internacao {
  +Long id
  +LocalDateTime dataEntrada
  +LocalDate dataPrevistaAlta
  +StatusInternacao status
}
class Quarto {
  <<abstract>>
  +String numeroIdentificacao
  +Integer capacidadeMaxima
  +Integer ocupacaoAtual
  +registrarAdmissao()
}
class Enfermaria
class Apartamento
class UnidadeTerapiaIntensiva
class QuartoIsolamento
Quarto <|-- Enfermaria
Quarto <|-- Apartamento
Quarto <|-- UnidadeTerapiaIntensiva
Quarto <|-- QuartoIsolamento

class HistoricoMedico {
  +List consultas
  +List internacoes
  +List procedimentosCirurgicos
}

Paciente "1" --> "0..*" Atendimento
ProfissionalSaude "1" --> "0..*" Atendimento
Paciente "1" --> "0..*" Internacao
ProfissionalSaude "1" --> "0..*" Internacao
Quarto "1" --> "0..*" Internacao
HistoricoMedico --> Paciente
HistoricoMedico o-- Consulta
HistoricoMedico o-- Internacao
HistoricoMedico o-- ProcedimentoCirurgico
```

## Persistência

| Hierarquia | Estratégia | Tabela(s) |
|---|---|---|
| Profissionais | `SINGLE_TABLE`, discriminador `categoria` | `profissionais_saude` |
| Consultas | `SINGLE_TABLE`, discriminador `modalidade` | `consultas` |
| Atendimento | `@MappedSuperclass` | sem tabela própria |
| Cirurgias | entidade concreta | `procedimentos_cirurgicos` |
| Quartos | `SINGLE_TABLE`, discriminador `tipo` | `quartos` |

`AgendaService` é o colaborador de domínio que impede sobreposição de intervalos entre consultas e cirurgias do mesmo profissional.
