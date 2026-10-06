# Sistema de Informação Hospitalar

Trabalho Prático desenvolvido para a disciplina de **Programação Modular** do curso de Engenharia de Software da **Pontifícia Universidade Católica de Minas Gerais (PUC Minas)**.

---

## 📌 Visão Geral do Sistema

O **Sistema de Informação Hospitalar** é uma aplicação desenvolvida em **Java** com o framework **Spring Boot**. O objetivo do sistema é fornecer uma API REST robusta, modular e de fácil manutenção para o gerenciamento dos fluxos hospitalares essenciais, incluindo:

* Cadastro e gestão de **pacientes**;
* Cadastro e gestão de **profissionais de saúde** (médicos, enfermeiros, etc.);
* Gerenciamento e controle de vagas em **quartos/leitos**;
* Agendamento, cancelamento e conclusão de **consultas ambulatoriais**;
* Controle de **internações hospitalares** e altas médicas;
* Consulta unificada do **histórico médico / prontuário** dos pacientes;
* Interface web conceitual (**front-end sem funcionalidade** para a Sprint 1).

---

## 🎨 Entregáveis da Sprint 1

A Sprint 1 contempla os seguintes artefatos exigidos:

1. **Front-end (Telas sem funcionalidade)**:
   * Interface visual construída em HTML5 e CSS3 (localizada em `src/main/resources/static/`).
   * Acessível diretamente pelo navegador em `http://localhost:8080/` ao executar a aplicação Spring Boot, ou abrindo diretamente o arquivo `index.html`.
   * Contempla telas conceituais para: *Visão Geral*, *Pacientes*, *Profissionais*, *Consultas*, *Internações*, *Quartos* e *Histórico Médico*.

2. **Diagrama de Classes**:
   * Documento com representação visual em Mermaid e código PlantUML disponível em [`docs/DIAGRAMA_CLASSES.md`](docs/DIAGRAMA_CLASSES.md).

3. **Cartões CRC**:
   * Cartões no modelo acadêmico (Classe, Responsabilidades, Colaborações) disponíveis em [`docs/CARTOES_CRC.md`](docs/CARTOES_CRC.md).

4. **Controllers Básicas (Responsabilidade da Sofia)**:
   * [`PacienteController`](src/main/java/com/hospital/controladores/PacienteController.java)
   * [`ProfissionalController`](src/main/java/com/hospital/controladores/ProfissionalController.java)

---

## 🏢 Arquitetura e Decisões Técnicas

O projeto adota uma **Arquitetura em Camadas (Layered Architecture)** com separação estrita de responsabilidades:

```
src/main/java/com/hospital/
├── controladores/     # Camada de Apresentação (REST Controllers)
├── servicos/          # Camada de Negócio (Regras, validações e transações)
├── repositorios/      # Camada de Acesso a Dados (Spring Data JPA)
├── entidades/         # Camada de Domínio / Entidades ORM (JPA)
├── dtos/              # Objetos de Transferência de Dados (Data Transfer Objects)
└── excecoes/          # Tratamento Global de Erros e Exceções Customizadas
```

### 1. Camada de Controladores (`controladores`)
* Responsável por expor os endpoints HTTP e receber as requisições REST.
* Injeção de dependências via construtor.
* Validação de entrada utilizando anotações do Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`).
* Retorna códigos de status HTTP adequados (`200 OK`, `201 CREATED`, `400 BAD REQUEST`, `404 NOT FOUND`).

### 2. Camada de Serviços (`servicos`)
* Centraliza toda a lógica de negócio (e.g., choque de horários e capacidade máxima de leitos).
* Controle transacional (`@Transactional` e `@Transactional(readOnly = true)`).
* Conversão entre entidades e DTOs.

### 3. Camada de Repositórios (`repositorios`)
* Interfaces estendendo `JpaRepository` com queries derivadas e JPQL.

### 4. Camada de Entidades (`entidades`)
* Entidades JPA mapeadas para banco relacional com enums para controle de estado.

### 5. Camada de DTOs (`dtos`)
* Objetos que isolam a camada de dados da camada de apresentação, evitando acoplamento excessivo.

### 6. Tratamento de Exceções (`excecoes`)
* Centralizado com `@RestControllerAdvice` (`GlobalExceptionHandler`), padronizando o payload JSON de resposta em caso de erros.

---

## ⚙️ Regras de Negócio do Sistema

1. **Pacientes (`PacienteService`)**:
   * CPF único no sistema. Campos obrigatórios: Nome, CPF e Data de Nascimento.

2. **Profissionais de Saúde (`ProfissionalSaudeService`)**:
   * Registro Profissional (CRM/COREN) único. Campos obrigatórios: Nome, Registro e Especialidade.

3. **Quartos (`QuartoService`)**:
   * Capacidade máxima de leitos e status (`DISPONIVEL`, `LOTADO`, `MANUTENCAO`).

4. **Consultas (`ConsultaService`)**:
   * Prevenção de choque de horários (janela mínima de 30 minutos por profissional).
   * Validação obrigatória da existência do paciente e do médico.

5. **Internações (`InternacaoService`)**:
   * Verificação de vaga disponível no quarto antes de internar.
   * Controle automático de ocupação (incremento na entrada e decremento na alta).
   * Bloqueio de internações simultâneas para o mesmo paciente.

6. **Histórico Médico (`HistoricoMedicoService`)**:
   * Consolidação de todas as consultas e internações do paciente em um único prontuário.

---

## 🚀 Endpoints da API REST

### 👤 Pacientes (`/pacientes`)
* `POST /pacientes` — Cadastrar um novo paciente.
* `GET /pacientes` — Listar todos os pacientes cadastrados.
* `GET /pacientes/{id}` — Buscar paciente por ID.

### 🩺 Profissionais de Saúde (`/profissionais`)
* `POST /profissionais` — Cadastrar um novo profissional de saúde.
* `GET /profissionais` — Listar todos os profissionais de saúde.
* `GET /profissionais/{id}` — Buscar profissional por ID.

### 🚪 Quartos (`/quartos`)
* `POST /quartos` — Cadastrar um novo quarto hospitalar.
* `GET /quartos` — Listar todos os quartos.
* `GET /quartos/disponiveis` — Listar quartos com leitos disponíveis.
* `GET /quartos/{id}` — Buscar quarto por ID.

### 📅 Consultas (`/consultas`)
* `POST /consultas` — Agendar nova consulta médica.
* `GET /consultas` — Listar todas as consultas.
* `GET /consultas/{id}` — Buscar consulta por ID.
* `GET /consultas/paciente/{pacienteId}` — Listar consultas de um paciente.
* `GET /consultas/profissional/{profissionalId}` — Listar consultas de um profissional.
* `PUT /consultas/{id}/cancelar` — Cancelar uma consulta.
* `PUT /consultas/{id}/finalizar` — Concluir atendimento com observações médicas.

### 🏥 Internações (`/internacoes`)
* `POST /internacoes` — Registrar internação hospitalar.
* `GET /internacoes` — Listar todas as internações.
* `GET /internacoes/em-andamento` — Listar internações ativas.
* `GET /internacoes/{id}` — Buscar internação por ID.
* `GET /internacoes/paciente/{pacienteId}` — Listar internações de um paciente.
* `PUT /internacoes/{id}/alta` — Dar alta médica ao paciente liberando o leito.

### 📂 Histórico Médico (`/historico`)
* `GET /historico/paciente/{pacienteId}` — Obter prontuário consolidado do paciente.

---

## 🛠️ Tecnologias e Dependências

* **Java 17** (LTS)
* **Spring Boot 3.2.3** (Spring Web, Spring Data JPA, Spring Validation)
* **H2 Database** (Banco de dados relacional em memória)
* **JUnit 5 & Mockito** (Testes unitários)
* **HTML5 / CSS3 / JavaScript** (Front-end conceitual sem funcionalidade)
* **Maven** (Gerenciador de dependências e build)

---

## 💻 Como Executar a Aplicação

### Pré-requisitos
* Java JDK 17 ou superior.
* Maven instalado (ou executar diretamente via IDE).

### Executar a API e o Front-end
```bash
mvn spring-boot:run
```

* **Front-end**: Acesse `http://localhost:8080/` no navegador.
* **Console H2**: Acesse `http://localhost:8080/h2-console`
  * JDBC URL: `jdbc:h2:mem:hospitaldb`
  * Usuário: `sa`
  * Senha: *(em branco)*

### Executar os Testes Unitários
```bash
mvn test
```