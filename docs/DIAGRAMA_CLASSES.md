# Diagrama de Classes — Sprint 2

Trabalho Prático de Programação Modular — Sistema de Informação Hospitalar  
Curso de Engenharia de Software — PUC Minas

---

## 📊 Diagrama Visual (Mermaid)

```mermaid
classDiagram
    direction TB

    class SituacaoConsulta {
        <<enumeration>>
        AGENDADA
        CONFIRMADA
        EM_ATENDIMENTO
        CONCLUIDA
        CANCELADA
        NAO_COMPARECEU
    }

    class ModalidadeAtendimento {
        <<enumeration>>
        ROTINA
        ESPECIALIZADA
        EMERGENCIA
    }

    class PrioridadeAtendimento {
        <<enumeration>>
        BAIXA
        MODERADA
        ALTA
        CRITICA
    }

    class StatusInternacao {
        <<enumeration>>
        EM_ANDAMENTO
        ALTA
        CANCELADA
    }

    class StatusQuarto {
        <<enumeration>>
        DISPONIVEL
        LOTADO
        MANUTENCAO
    }

    class Paciente {
        -Long id
        -String nome
        -String cpf
        -LocalDate dataNascimento
        -String telefone
        -String endereco
        -String email
        +getId() Long
        +getNome() String
        +setNome(String nome) void
        +getCpf() String
        +setCpf(String cpf) void
        +getDataNascimento() LocalDate
        +setDataNascimento(LocalDate dataNascimento) void
        +getTelefone() String
        +setTelefone(String telefone) void
        +getEndereco() String
        +setEndereco(String endereco) void
        +getEmail() String
        +setEmail(String email) void
    }

    class ProfissionalSaude {
        -Long id
        -String nome
        -String registroProfissional
        -String especialidade
        -String telefone
        -String email
        +getId() Long
        +getNome() String
        +setNome(String nome) void
        +getRegistroProfissional() String
        +setRegistroProfissional(String registro) void
        +getEspecialidade() String
        +setEspecialidade(String especialidade) void
        +getTelefone() String
        +setTelefone(String telefone) void
        +getEmail() String
        +setEmail(String email) void
    }

    class Quarto {
        -Long id
        -String numero
        -Integer capacidadeMaxima
        -Integer ocupacaoAtual
        -StatusQuarto status
        +getId() Long
        +getNumero() String
        +getCapacidadeMaxima() Integer
        +getOcupacaoAtual() Integer
        +getStatus() StatusQuarto
        +temVagaDisponivel() boolean
        +incrementarOcupacao() void
        +decrementarOcupacao() void
    }

    class Consulta {
        <<abstract>>
        -Long id
        -Paciente paciente
        -ProfissionalSaude profissional
        -LocalDateTime dataHora
        -String motivoConsulta
        -String observacoesMedicas
        -SituacaoConsulta situacao
        +getId() Long
        +getPaciente() Paciente
        +getProfissional() ProfissionalSaude
        +getDataHora() LocalDateTime
        +getSituacao() SituacaoConsulta
        +getModalidade() ModalidadeAtendimento
        +validarCondicoesAtendimento() void
        +transitarPara(SituacaoConsulta nova) void
        +confirmar() void
        +iniciarAtendimento() void
        +concluir(String obs) void
        +cancelar() void
        +registrarNaoComparecimento() void
    }

    class ConsultaRotina {
        +getModalidade() ModalidadeAtendimento
        +validarCondicoesAtendimento() void
    }

    class ConsultaEspecializada {
        -String especialidade
        +getEspecialidade() String
        +setEspecialidade(String esp) void
        +getModalidade() ModalidadeAtendimento
        +validarCondicoesAtendimento() void
    }

    class AtendimentoEmergencia {
        -PrioridadeAtendimento prioridade
        +getPrioridade() PrioridadeAtendimento
        +setPrioridade(PrioridadeAtendimento p) void
        +getModalidade() ModalidadeAtendimento
        +validarCondicoesAtendimento() void
        +isAguardandoAvaliacao() boolean
        +isEmAtendimento() boolean
        +isConcluido() boolean
    }

    class Internacao {
        -Long id
        -Paciente paciente
        -Quarto quarto
        -LocalDateTime dataEntrada
        -LocalDateTime dataAlta
        -StatusInternacao status
        -String motivo
        +getId() Long
        +getPaciente() Paciente
        +getQuarto() Quarto
        +getDataEntrada() LocalDateTime
        +getDataAlta() LocalDateTime
        +setDataAlta(LocalDateTime dataAlta) void
        +getStatus() StatusInternacao
        +setStatus(StatusInternacao status) void
        +getMotivo() String
    }

    Consulta <|-- ConsultaRotina : herda
    Consulta <|-- ConsultaEspecializada : herda
    Consulta <|-- AtendimentoEmergencia : herda

    Paciente "1" -- "0..*" Consulta : possui
    ProfissionalSaude "1" -- "0..*" Consulta : realiza
    Paciente "1" -- "0..*" Internacao : internado em
    Quarto "1" -- "0..*" Internacao : aloca

    Consulta --> SituacaoConsulta
    Consulta --> ModalidadeAtendimento
    AtendimentoEmergencia --> PrioridadeAtendimento
    Internacao --> StatusInternacao
    Quarto --> StatusQuarto
```

---

## 📝 Código PlantUML

```plantuml
@startuml
skinparam classAttributeIconSize 0

enum SituacaoConsulta {
  AGENDADA
  CONFIRMADA
  EM_ATENDIMENTO
  CONCLUIDA
  CANCELADA
  NAO_COMPARECEU
}

enum ModalidadeAtendimento {
  ROTINA
  ESPECIALIZADA
  EMERGENCIA
}

enum PrioridadeAtendimento {
  BAIXA
  MODERADA
  ALTA
  CRITICA
}

enum StatusInternacao {
  EM_ANDAMENTO
  ALTA
  CANCELADA
}

enum StatusQuarto {
  DISPONIVEL
  LOTADO
  MANUTENCAO
}

class Paciente {
  - id: Long
  - nome: String
  - cpf: String
  - dataNascimento: LocalDate
  - telefone: String
  - endereco: String
  - email: String
  + getId(): Long
  + getNome(): String
  + setNome(nome: String): void
  + getCpf(): String
  + setCpf(cpf: String): void
  + getDataNascimento(): LocalDate
  + setDataNascimento(data: LocalDate): void
  + getTelefone(): String
  + setTelefone(telefone: String): void
  + getEndereco(): String
  + setEndereco(endereco: String): void
  + getEmail(): String
  + setEmail(email: String): void
}

class ProfissionalSaude {
  - id: Long
  - nome: String
  - registroProfissional: String
  - especialidade: String
  - telefone: String
  - email: String
  + getId(): Long
  + getNome(): String
  + setNome(nome: String): void
  + getRegistroProfissional(): String
  + setRegistroProfissional(registro: String): void
  + getEspecialidade(): String
  + setEspecialidade(esp: String): void
  + getTelefone(): String
  + setTelefone(telefone: String): void
  + getEmail(): String
  + setEmail(email: String): void
}

class Quarto {
  - id: Long
  - numero: String
  - capacidadeMaxima: Integer
  - ocupacaoAtual: Integer
  - status: StatusQuarto
  + getId(): Long
  + getNumero(): String
  + getCapacidadeMaxima(): Integer
  + getOcupacaoAtual(): Integer
  + getStatus(): StatusQuarto
  + temVagaDisponivel(): boolean
  + incrementarOcupacao(): void
  + decrementarOcupacao(): void
}

abstract class Consulta {
  - id: Long
  - paciente: Paciente
  - profissional: ProfissionalSaude
  - dataHora: LocalDateTime
  - motivoConsulta: String
  - observacoesMedicas: String
  - situacao: SituacaoConsulta
  + getId(): Long
  + getPaciente(): Paciente
  + getProfissional(): ProfissionalSaude
  + getDataHora(): LocalDateTime
  + getSituacao(): SituacaoConsulta
  + getModalidade(): ModalidadeAtendimento
  + validarCondicoesAtendimento(): void
  + transitarPara(nova: SituacaoConsulta): void
  + confirmar(): void
  + iniciarAtendimento(): void
  + concluir(obs: String): void
  + cancelar(): void
  + registrarNaoComparecimento(): void
}

class ConsultaRotina {
  + getModalidade(): ModalidadeAtendimento
  + validarCondicoesAtendimento(): void
}

class ConsultaEspecializada {
  - especialidade: String
  + getEspecialidade(): String
  + setEspecialidade(esp: String): void
  + getModalidade(): ModalidadeAtendimento
  + validarCondicoesAtendimento(): void
}

class AtendimentoEmergencia {
  - prioridade: PrioridadeAtendimento
  + getPrioridade(): PrioridadeAtendimento
  + setPrioridade(p: PrioridadeAtendimento): void
  + getModalidade(): ModalidadeAtendimento
  + validarCondicoesAtendimento(): void
  + isAguardandoAvaliacao(): boolean
  + isEmAtendimento(): boolean
  + isConcluido(): boolean
}

class Internacao {
  - id: Long
  - paciente: Paciente
  - quarto: Quarto
  - dataEntrada: LocalDateTime
  - dataAlta: LocalDateTime
  - status: StatusInternacao
  - motivo: String
  + getId(): Long
  + getPaciente(): Paciente
  + getQuarto(): Quarto
  + getDataEntrada(): LocalDateTime
  + getDataAlta(): LocalDateTime
  + setDataAlta(data: LocalDateTime): void
  + getStatus(): StatusInternacao
  + setStatus(status: StatusInternacao): void
  + getMotivo(): String
}

Consulta <|-- ConsultaRotina
Consulta <|-- ConsultaEspecializada
Consulta <|-- AtendimentoEmergencia

Paciente "1" -- "0..*" Consulta : possui >
ProfissionalSaude "1" -- "0..*" Consulta : realiza >
Paciente "1" -- "0..*" Internacao : internado em >
Quarto "1" -- "0..*" Internacao : aloca >

Consulta --> SituacaoConsulta
Consulta --> ModalidadeAtendimento
AtendimentoEmergencia --> PrioridadeAtendimento
Internacao --> StatusInternacao
Quarto --> StatusQuarto

@enduml
```

---

## 📌 Detalhamento dos Relacionamentos e Multiplicidades

1. **Paciente — Consulta (1 para 0..*)**:
   * Um `Paciente` pode ter 0 ou várias consultas registradas em seu histórico.
   * Toda `Consulta` pertence obrigatoriamente a exatamente 1 paciente.

2. **ProfissionalSaude — Consulta (1 para 0..*)**:
   * Um `ProfissionalSaude` pode atender 0 ou várias consultas ao longo do tempo.
   * Toda `Consulta` é conduzida por exatamente 1 profissional responsável.

3. **Herança de Modalidades de Atendimento**:
   * `Consulta` é a classe base abstrata/generalizada.
   * `ConsultaRotina`, `ConsultaEspecializada` e `AtendimentoEmergencia` estendem `Consulta`, implementando regras de validação polimórficas (`validarCondicoesAtendimento()`).

4. **Paciente — Internacao (1 para 0..*)**:
   * Um `Paciente` pode possuir 0 ou mais internações no seu histórico clínico.
   * Cada registro de `Internacao` refere-se a exatamente 1 paciente.

5. **Quarto — Internacao (1 para 0..*)**:
   * Um `Quarto` pode receber múltiplas internações ao longo de sua vida útil, respeitando a capacidade máxima simultânea (`ocupacaoAtual <= capacidadeMaxima`).
   * Cada `Internacao` está associada a exatamente 1 quarto.
