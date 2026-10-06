# Sistema de Informação Hospitalar

Trabalho Prático desenvolvido para a disciplina de **Programação Modular** do curso de Engenharia de Software da **Pontifícia Universidade Católica de Minas Gerais (PUC Minas)**.

---

## 📌 Visão Geral do Sistema

O **Sistema de Informação Hospitalar** é uma aplicação backend desenvolvida em **Java** com o framework **Spring Boot**. O objetivo do sistema é fornecer uma API REST robusta, modular e de fácil manutenção para o gerenciamento dos fluxos hospitalares essenciais, incluindo:

* Cadastro e gestão de **pacientes**;
* Cadastro e gestão de **profissionais de saúde** (médicos, enfermeiros, etc.);
* Gerenciamento e controle de vagas em **quartos/leitos**;
* Agendamento, cancelamento e conclusão de **consultas ambulatoriais**;
* Controle de **internações hospitalares** e altas médicas;
* Consulta unificada do **histórico médico / prontuário** dos pacientes.

---

## 🏢 Arquitetura e Decisões Técnicas

O projeto adota uma **Arquitetura em Camadas (Layered Architecture)**, amplamente utilizada no ecossistema Spring, com separação estrita de responsabilidades:

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
* Utiliza injeção de dependências via construtor (boa prática recomendada).
* Realiza validação de entrada utilizando as anotações do Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`).
* Delega a execução para a camada de serviço correspondente e retorna os códigos de status HTTP apropriados (`200 OK`, `201 CREATED`, `400 BAD REQUEST`, `404 NOT FOUND`).

### 2. Camada de Serviços (`servicos`)
* Centraliza toda a lógica de negócio, garantindo que regras como choque de horários e capacidade de leitos nunca sejam violadas.
* Controla a demarcação transacional (`@Transactional` e `@Transactional(readOnly = true)`).
* Converte entidades em DTOs para envio à camada de apresentação.

### 3. Camada de Repositórios (`repositorios`)
* Interfaces que estendem `JpaRepository`, permitindo operações de CRUD e paginação sem código boilerplate.
* Contém métodos de consulta derivados (e.g. `existsByCpf`, `existsByRegistroProfissional`) e JPQL quando necessário.

### 4. Camada de Entidades (`entidades`)
* Representa o modelo relacional do banco de dados mapeado com JPA/Hibernate.
* Possui chaves primárias autoincrementais (`@GeneratedValue(strategy = GenerationType.IDENTITY)`), restrições de unicidade e enums de status.

### 5. Camada de DTOs (`dtos`)
* Isola a estrutura interna do banco de dados das interfaces expostas externamente na API.
* Evita problemas de referência circular no JSON e vazamento de dados sensíveis ou colunas internas.

### 6. Tratamento de Exceções (`excecoes`)
* Utiliza um `@RestControllerAdvice` (`GlobalExceptionHandler`) para interceptar exceções de negócio e validação, padronizando a resposta JSON de erro com `timestamp`, `status`, `error` e detalhes amigáveis.

---

## ⚙️ Regras de Negócio Implementadas

1. **Pacientes (`PacienteService`)**:
   * O CPF deve ser único no sistema. Não é permitido cadastrar dois pacientes com o mesmo CPF.
   * Campos obrigatórios: Nome, CPF e Data de Nascimento.

2. **Profissionais de Saúde (`ProfissionalSaudeService`)**:
   * O Registro Profissional (CRM/COREN) deve ser único.
   * Campos obrigatórios: Nome, Registro Profissional e Especialidade.

3. **Quartos (`QuartoService`)**:
   * Cada quarto possui uma capacidade máxima de leitos e status (`DISPONIVEL`, `LOTADO`, `MANUTENCAO`).
   * O sistema impede internações quando a ocupação atinge a capacidade.

4. **Consultas (`ConsultaService`)**:
   * **Prevenção de Choque de Horários**: Um profissional não pode ter dois atendimentos marcados com sobreposição de horário (janela mínima de 30 minutos).
   * Validação obrigatória da existência prévia do paciente e do profissional no banco.

5. **Internações (`InternacaoService`)**:
   * Valida a capacidade do quarto antes de internar.
   * Incrementa automaticamente a ocupação do quarto ao internar e decrementa ao conceder alta hospitalar.
   * Impede que um paciente seja internado em duplicidade se já possuir internação em andamento.

6. **Histórico Médico (`HistoricoMedicoService`)**:
   * Agrega em um único payload todas as consultas (agendadas, realizadas ou canceladas) e todas as internações de determinado paciente.

---

## 🚀 Endpoints da API REST

### 👤 Pacientes (`/pacientes`)
* `POST /pacientes` — Cadastrar um novo paciente.
* `GET /pacientes` — Listar todos os pacientes cadastrados.
* `GET /pacientes/{id}` — Buscar dados detalhados de um paciente por ID.

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
* `PUT /consultas/{id}/finalizar` — Concluir atendimento e adicionar observações médicas.

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
* **Spring Boot 3.2.3**
  * `spring-boot-starter-web` (API REST / MVC)
  * `spring-boot-starter-data-jpa` (Persistência e ORM)
  * `spring-boot-starter-validation` (Bean Validation)
* **H2 Database** (Banco de dados relacional em memória)
* **JUnit 5 & Mockito** (Testes unitários automatizados)
* **Maven** (Gerenciador de dependências e build)

---

## 💻 Como Executar a Aplicação

### Pré-requisitos
* Java JDK 17 ou superior instalado.
* Maven configurado no PATH (ou IDE como IntelliJ IDEA / Eclipse / VS Code).

### Execução via Linha de Comando
```bash
# Compilar e rodar a aplicação
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### Console do Banco H2
Com a aplicação em execução, acesse o console web do H2 em:
* **URL**: `http://localhost:8080/h2-console`
* **JDBC URL**: `jdbc:h2:mem:hospitaldb`
* **User**: `sa`
* **Password**: *(deixar em branco)*

### Execução dos Testes Unitários
```bash
mvn test
```oot 3.2.3** (Spring Web, Spring Data JPA, Spring Validation)
- **H2 Database** (Banco em memória para desenvolvimento e testes)
- **JUnit 5 & Mockito** (Testes unitários)