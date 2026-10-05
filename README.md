# Sistema de Informação Hospitalar

Trabalho Prático para a disciplina de **Programação Modular** da **Pontifícia Universidade Católica de Minas Gerais (PUC Minas)**.

---

## 📌 Visão Geral do Sistema

O Sistema de Informação Hospitalar foi desenvolvido utilizando a plataforma Java com Spring Boot. A aplicação provê uma API REST completa para a gestão de operações hospitalares, contemplando a administração de pacientes, profissionais de saúde, quartos, agendamento de consultas, gerenciamento de internações e consulta ao histórico médico dos pacientes.

---

## 🏢 Arquitetura da Aplicação

A aplicação foi estruturada em uma **arquitetura em camadas** bem definida, garantindo desacoplamento, testabilidade e separação de responsabilidades:

1. **Controller (`com.hospital.controladores`)**:
   - Exposição das rotas REST HTTP (`/pacientes`, `/profissionais`, `/quartos`, `/consultas`, `/internacoes`, `/historico`).
   - Mapeamento e validação dos dados de entrada (`@Valid`).

2. **Service (`com.hospital.servicos`)**:
   - Contém toda a lógica de negócio e validação das regras do hospital.
   - Gerenciamento de transações (`@Transactional`).

3. **Repository (`com.hospital.repositorios`)**:
   - Interfaces estendendo `JpaRepository` para comunicação com o banco de dados.
   - Definição de métodos de busca customizados e queries JPQL.

4. **Model / Domain (`com.hospital.entidades`)**:
   - Entidades JPA (`Paciente`, `ProfissionalSaude`, `Quarto`, `Consulta`, `Internacao`).
   - Enums para controle de status (`StatusConsulta`, `StatusInternacao`, `StatusQuarto`).

5. **DTO (`com.hospital.dtos`)**:
   - Objetos de transferência de dados para isolar as entidades JPA da camada de apresentação da API REST.

6. **Exception (`com.hospital.excecoes`)**:
   - Tratamento centralizado de erros com `@RestControllerAdvice` (`GlobalExceptionHandler`).

---

## ⚙️ Regras de Negócio Implementadas

### 1. Agendamento de Consultas (`ConsultaService`)
- **Choque de Horários**: Um profissional de saúde não pode possuir dois atendimentos agendados para o mesmo horário (ou em janela de sobreposição inferior a 30 minutos).
- **Consistência de Dados**: Validação da existência prévia do paciente e do profissional cadastrados.

### 2. Controle de Internações e Quartos (`InternacaoService` & `QuartoService`)
- **Validação de Capacidade**: Uma internação só é permitida se o quarto selecionado possuir leito disponível (`ocupacaoAtual < capacidadeMaxima`).
- **Incremento/Decremento de Ocupação**: Ao realizar a internação, o sistema incrementa automaticamente a ocupação do quarto. Na alta do paciente (`PUT /internacoes/{id}/alta`), a ocupação é decrementada.
- **Internação Única por Paciente**: Um paciente não pode ser internado novamente se já possuir uma internação em andamento.

### 3. Prontuário e Histórico Médico (`HistoricoMedicoService`)
- Consolidação de todas as consultas e internações realizadas por um paciente específico via endpoint `/historico/paciente/{pacienteId}`.

---

## 🚀 Endpoints da API

### 🏥 Consultas (`/consultas`)
- `POST /consultas` — Agendar nova consulta.
- `GET /consultas` — Listar todas as consultas.
- `GET /consultas/{id}` — Buscar consulta por ID.
- `GET /consultas/paciente/{pacienteId}` — Consultas por paciente.
- `GET /consultas/profissional/{profissionalId}` — Consultas por médico.
- `PUT /consultas/{id}/cancelar` — Cancelar consulta.
- `PUT /consultas/{id}/finalizar` — Finalizar consulta com observações.

### 🛌 Internações (`/internacoes`)
- `POST /internacoes` — Registrar internação.
- `GET /internacoes` — Listar todas as internações.
- `GET /internacoes/em-andamento` — Listar internações ativas.
- `GET /internacoes/{id}` — Buscar por ID.
- `GET /internacoes/paciente/{pacienteId}` — Internações do paciente.
- `PUT /internacoes/{id}/alta` — Dar alta hospitalar ao paciente.

### 📂 Histórico Médico (`/historico`)
- `GET /historico/paciente/{pacienteId}` — Consultar prontuário e histórico completo.

---

## 🧪 Executando os Testes

Para executar a suíte de testes unitários desenvolvida para a aplicação:

```bash
mvn test
```

---

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.3** (Spring Web, Spring Data JPA, Spring Validation)
- **H2 Database** (Banco em memória para desenvolvimento e testes)
- **JUnit 5 & Mockito** (Testes unitários)