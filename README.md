# Sistema de Informação Hospitalar

Trabalho Prático desenvolvido para a disciplina de **Programação Modular** do curso de Engenharia de Software da **Pontifícia Universidade Católica de Minas Gerais (PUC Minas)**.
---

## 📌 Visão Geral do Sistema

O **Sistema de Informação Hospitalar** é uma aplicação desenvolvida em **Java** com o framework **Spring Boot**. O objetivo do sistema é fornecer uma API REST robusta, modular e de fácil manutenção para o gerenciamento dos fluxos hospitalares essenciais, incluindo:

* Cadastro e gestão de **pacientes**;
* Cadastro e gestão de **profissionais de saúde** (médicos clínicos, especialistas e cirurgiões);
* Gerenciamento de **quartos/leitos** por tipo (enfermaria, apartamento, UTI e isolamento), com controle de vagas, manutenção e painel de ocupação;
* Agendamento, confirmação, atendimento e controle de **consultas ambulatoriais e atendimentos de emergência**;
* Controle de **internações hospitalares** e altas médicas;
* Gestão de **procedimentos cirúrgicos**;
* Consulta unificada do **histórico médico / prontuário** dos pacientes;
* Interface web responsiva e funcional, integrada aos endpoints das Sprints 1 e 2.

---

## 🎨 Entregáveis da Sprint 1

A Sprint 1 contempla os seguintes artefatos exigidos:

1. **Front-end funcional**:
   * Interface visual construída em HTML5 e CSS3 (localizada em `src/main/resources/static/`).
   * Acessível diretamente pelo navegador em `http://localhost:8080/` ao executar a aplicação Spring Boot, ou abrindo diretamente o arquivo `index.html`.
   * Contempla telas integradas para: *Visão Geral*, *Pacientes*, *Profissionais*, *Consultas*, *Internações*, *Quartos* e *Histórico Médico*.

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
* Entidades JPA mapeadas para banco relacional com herança polimórfica (`SINGLE_TABLE`) e enums para controle de estado.

### 5. Camada de DTOs (`dtos`)
* Objetos que isolam a camada de dados da camada de apresentação, evitando acoplamento excessivo.

### 6. Tratamento de Exceções (`excecoes`)
* Centralizado com `@RestControllerAdvice` (`GlobalExceptionHandler`), que estende `ResponseEntityExceptionHandler` e devolve sempre o mesmo JSON (`RespostaErro`: `timestamp`, `status`, `error`, `message`, `path` e, em erros de validação, `errors` por campo).

  | Código | Situação |
  |---|---|
  | `400` | Regra de negócio violada, campos inválidos, JSON malformado, parâmetro com valor inválido |
  | `404` | Recurso ou rota inexistente |
  | `405` | Método HTTP não permitido |
  | `409` | Choque de horário, quarto lotado ou em manutenção, violação de integridade, conflito de concorrência |
  | `500` | Erro inesperado (detalhes apenas no log do servidor) |

---

## ⚙️ Regras de Negócio do Sistema

1. **Pacientes (`PacienteService`)**:
   * Campos obrigatórios: Nome, CPF e Data de Nascimento.
   * CPF validado pelos dígitos verificadores, aceito com ou sem máscara e sempre gravado como `000.000.000-00` (classe `Cpf`). CPF único no sistema, também na atualização.
   * Data de nascimento no passado e idade máxima de 130 anos. A idade é calculada e devolvida pela API.
   * Pacientes com consultas, internações ou cirurgias registradas **não podem ser excluídos** (preservação do histórico).

2. **Profissionais de Saúde (`ProfissionalSaudeService`)**:
   * Registro Profissional (CRM/COREN) único. Campos obrigatórios: Nome, Registro e Especialidade.

3. **Quartos (`QuartoService`) — Herança e Polimorfismo**:
   * `Quarto` é abstrato; cada tipo é uma subclasse criada pela fábrica `TipoQuarto.novoQuarto()`:

     | Tipo | Classe | Leitos | Regra específica |
     |---|---|---|---|
     | `ENFERMARIA` | `Enfermaria` | 2 a 8 | — |
     | `APARTAMENTO` | `Apartamento` | 1 a 2 | Atributo `permiteAcompanhante` (padrão: sim) |
     | `UTI` | `UnidadeTerapiaIntensiva` | 1 a 10 | Internação exige justificativa clínica nas observações |
     | `ISOLAMENTO` | `QuartoIsolamento` | exatamente 1 | Tipo de isolamento obrigatório (`CONTATO`, `GOTICULAS`, `AEROSSOIS`) |
   * Situação (`StatusQuarto`): `DISPONIVEL` e `OCUPADO` são calculadas automaticamente pela ocupação; `EM_MANUTENCAO` é definida manualmente.
   * A ocupação nunca ultrapassa a capacidade máxima: o próprio quarto valida a admissão (`registrarAdmissao`, padrão *Template Method*) e lança `QuartoLotadoException`.
   * Só quartos vazios entram em manutenção; quartos em manutenção não recebem internações.
   * A capacidade não pode ser reduzida abaixo da ocupação atual nem sair da faixa do tipo.
   * Número de identificação único. Quartos com internações registradas não podem ser excluídos.
   * Controle de concorrência otimista (`@Version`): duas internações simultâneas no último leito não ultrapassam a capacidade.

4. **Consultas e Atendimentos (`ConsultaService`)**:
   * Prevenção de choque de horários (janela mínima de 30 minutos por profissional).
   * Validação obrigatória da existência do paciente e do médico responsável.
   * **Modalidades de Atendimento (Herança e Polimorfismo)**:
     * **Consulta de Rotina**: Destinada à avaliação geral do paciente. Requer motivo da consulta e permite anotações médicas após a realização.
     * **Consulta Especializada**: Associada a uma especialidade médica. Exige obrigatoriamente que a área de atuação do médico seja compatível com a especialidade solicitada.
     * **Atendimento de Emergência**: Atendimento imediato classificado por prioridade (`BAIXA`, `MODERADA`, `ALTA`, `CRITICA`). Permite identificar pacientes aguardando avaliação versus já em atendimento ou concluídos.
   * **Máquina de Situações da Consulta (`SituacaoConsulta`)**:
     * Estados: `AGENDADA`, `CONFIRMADA`, `EM_ATENDIMENTO`, `CONCLUIDA`, `CANCELADA`, `NAO_COMPARECEU`.
     * Transições controladas: apenas consultas em atendimento podem ser concluídas; consultas agendadas ou confirmadas podem ser canceladas ou marcadas como não compareceu; consultas finalizadas (`CONCLUIDA`, `CANCELADA`, `NAO_COMPARECEU`) não permitem alteração de status.

5. **Internações (`InternacaoService`)**:
   * Verificação de vaga e dos requisitos do tipo de quarto antes de internar (feita pelo próprio `Quarto`).
   * A data prevista de alta não pode ser anterior à data de entrada.
   * Controle automático de ocupação (incremento na entrada e decremento na alta).
   * Bloqueio de internações simultâneas para o mesmo paciente.

6. **Histórico Médico (`HistoricoMedicoService`)**:
   * Consolidação de todas as consultas, internações e procedimentos cirúrgicos do paciente em um único prontuário.

---

## 🚀 Endpoints da API REST

### 👤 Pacientes (`/pacientes`)
* `POST /pacientes` — Cadastrar um novo paciente.
* `GET /pacientes` — Listar todos os pacientes (ordem alfabética). Filtro opcional: `?nome=` (parte do nome).
* `GET /pacientes/{id}` — Buscar paciente por ID.
* `GET /pacientes/cpf/{cpf}` — Buscar paciente por CPF (com ou sem máscara).
* `PUT /pacientes/{id}` — Atualizar os dados do paciente.
* `DELETE /pacientes/{id}` — Excluir paciente (somente sem histórico).

### 🩺 Profissionais de Saúde (`/profissionais`)
* `POST /profissionais` — Cadastrar um novo profissional de saúde.
* `GET /profissionais` — Listar todos os profissionais de saúde.
* `GET /profissionais/{id}` — Buscar profissional por ID.
* `GET /profissionais/especialidade/{especialidade}` — Pesquisar especialistas por área.
* `PUT /profissionais/{id}` — Atualizar os dados do profissional sem alterar sua categoria.
* `DELETE /profissionais/{id}` — Excluir profissional somente quando não houver histórico associado.

### 🚪 Quartos (`/quartos`)
* `POST /quartos` — Cadastrar um novo quarto. Corpo: `numeroIdentificacao`, `andar`, `capacidadeMaxima`, `tipo` (padrão `ENFERMARIA`) e, conforme o tipo, `permiteAcompanhante` ou `tipoIsolamento`.
* `GET /quartos` — Listar quartos. Filtros opcionais: `?tipo=UTI&andar=2&situacao=DISPONIVEL`.
* `GET /quartos/disponiveis` — Listar quartos com leitos livres. Filtro opcional: `?tipo=`.
* `GET /quartos/ocupacao` — Painel de ocupação (leitos totais, ocupados, disponíveis e taxa de ocupação, geral e por tipo).
* `GET /quartos/{id}` — Buscar quarto por ID.
* `GET /quartos/{id}/internacoes` — Internações em andamento no quarto (quem ocupa os leitos).
* `PUT /quartos/{id}` — Atualizar número, andar, capacidade e atributos do tipo.
* `PUT /quartos/{id}/manutencao` — Colocar o quarto (vazio) em manutenção.
* `PUT /quartos/{id}/liberar` — Liberar o quarto da manutenção.
* `DELETE /quartos/{id}` — Excluir quarto (somente se nunca teve internações).

### 📅 Consultas (`/consultas`)
* `POST /consultas` — Agendar nova consulta médica (rotina, especializada ou emergência).
* `GET /consultas` — Listar todas as consultas.
* `GET /consultas/{id}` — Buscar consulta por ID.
* `GET /consultas/paciente/{pacienteId}` — Listar consultas de um paciente.
* `GET /consultas/profissional/{profissionalId}` — Listar consultas de um profissional.
* `GET /consultas/situacao/{situacao}` — Filtrar consultas por situação (`AGENDADA`, `CONFIRMADA`, `EM_ATENDIMENTO`, etc.).
* `GET /consultas/modalidade/{modalidade}` — Filtrar consultas por modalidade (`ROTINA`, `ESPECIALIZADA`, `EMERGENCIA`).
* `GET /consultas/emergencias/aguardando` — Listar atendimentos de emergência aguardando avaliação médica.
* `PUT /consultas/{id}/confirmar` — Confirmar presença na consulta agendada.
* `PUT /consultas/{id}/iniciar` — Iniciar atendimento médico (passa para `EM_ATENDIMENTO`).
* `PUT /consultas/{id}/concluir` — Concluir atendimento médico registrando observações clínicas.
* `PUT /consultas/{id}/finalizar` — Alias/compatibilidade para conclusão do atendimento.
* `PUT /consultas/{id}/nao-compareceu` — Registrar não comparecimento do paciente.
* `PUT /consultas/{id}/cancelar` — Cancelar uma consulta.
* `PUT /consultas/{id}` — Atualizar os dados de uma consulta ainda não finalizada.
* `DELETE /consultas/{id}` — Cancelar logicamente e preservar o histórico.

### 🏥 Internações (`/internacoes`)
* `POST /internacoes` — Registrar internação hospitalar.
* `GET /internacoes` — Listar todas as internações.
* `GET /internacoes/em-andamento` — Listar internações ativas.
* `GET /internacoes/status/{status}` — Filtrar por situação, inclusive `CANCELADA`.
* `GET /internacoes/{id}` — Buscar internação por ID.
* `GET /internacoes/paciente/{pacienteId}` — Listar internações de um paciente.
* `PUT /internacoes/{id}/alta` — Dar alta médica ao paciente liberando o leito.
* `PUT /internacoes/{id}/cancelar` — Cancelar internação ativa liberando o leito.
* `PUT /internacoes/{id}` — Atualizar observações e previsão de alta da internação.
* `DELETE /internacoes/{id}` — Cancelar logicamente e preservar o histórico.

### 🔬 Procedimentos Cirúrgicos (`/procedimentos`)
* `POST /procedimentos` — Agendar novo procedimento cirúrgico.
* `GET /procedimentos` — Listar todos os procedimentos cirúrgicos.
* `GET /procedimentos/{id}` — Buscar procedimento cirúrgico por ID.
* `GET /procedimentos/paciente/{pacienteId}` — Listar procedimentos cirúrgicos de um paciente.
* `GET /procedimentos/cirurgiao/{cirurgiaoId}` — Listar procedimentos cirúrgicos de um cirurgião.
* `PUT /procedimentos/{id}/iniciar` — Iniciar cirurgia agendada (passa para EM_ANDAMENTO).
* `PUT /procedimentos/{id}/concluir` — Concluir cirurgia em andamento com horário de término e observações.
* `PUT /procedimentos/{id}/cancelar` — Cancelar cirurgia agendada ou em andamento.

### 📂 Histórico Médico (`/historico`)
* `GET /historico/paciente/{pacienteId}` — Obter prontuário consolidado do paciente.

---

## 🛠️ Tecnologias e Dependências

* **Java 17** (LTS)
* **Spring Boot 3.2.3** (Spring Web, Spring Data JPA, Spring Validation)
* **PostgreSQL (Supabase)** (Banco de dados relacional em produção/desenvolvimento — justificativa da escolha em [`docs/BANCO_DE_DADOS.md`](docs/BANCO_DE_DADOS.md))
* **H2 Database** (Banco de dados em memória para execução isolada e rápida da suíte de testes)
* **JUnit 5, Mockito & MockMvc** (Testes unitários de serviços e testes automatizados de integração da API REST)
* **HTML5 / CSS3 / JavaScript** (front-end responsivo integrado à API REST)
* **Maven** (Gerenciador de dependências e build)

---

## 💻 Como Executar a Aplicação

### Pré-requisitos
* Java JDK 17 ou superior.
* Maven instalado (ou executar diretamente via IDE).

### Configuração do Banco de Dados (PostgreSQL / Supabase)
A aplicação está configurada para conectar ao PostgreSQL hospedado no Supabase (`shluawyyxdnitwcaagwx`). As propriedades no `src/main/resources/application.properties` podem ser customizadas diretamente ou via variáveis de ambiente:

* `DB_URL`: URL JDBC do PostgreSQL (padrão: `jdbc:postgresql://db.shluawyyxdnitwcaagwx.supabase.co:5432/postgres?sslmode=require`)
* `DB_USERNAME`: Usuário do banco (padrão: `postgres`)
* `DB_PASSWORD`: Senha do banco definida no Supabase
> **Banco criado em versões anteriores?** Execute [`docs/sql/ajuste_banco_sprint2.sql`](docs/sql/ajuste_banco_sprint2.sql) no SQL Editor do Supabase (detalhes em [`docs/BANCO_DE_DADOS.md`](docs/BANCO_DE_DADOS.md)).

### Executar a API e o Front-end
```bash
mvn spring-boot:run
```

Sem acesso ao Supabase, use o perfil `h2` (banco em memória, zerado a cada execução; console em `http://localhost:8080/h2-console`):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

* **Front-end**: Acesse `http://localhost:8080/` no navegador.
* **API REST**: Endpoints disponíveis na porta `8080` (ex: `/pacientes`, `/profissionais`, `/consultas`, `/internacoes`, `/quartos`, `/historico`).

### Executar os Testes Automatizados (Serviços e API REST)
Os testes utilizam automaticamente o banco H2 em memória, garantindo execução rápida, idempotente e sem dependência de rede. A suíte inclui testes unitários (entidades e services, com Mockito), testes de controller (`@WebMvcTest`), testes de repositório (`@DataJpaTest`) e testes de integração da API completa (`HospitalApiIntegrationTest`, com `@SpringBootTest`), que também garantem que a aplicação consegue iniciar:
```bash
mvn test
```
