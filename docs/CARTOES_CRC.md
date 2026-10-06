# Cartões CRC (Class-Responsibility-Collaboration)

Trabalho Prático de Programação Modular — Sistema de Informação Hospitalar  
Curso de Engenharia de Software — PUC Minas

---

**Paciente**

### Responsabilidades
1. Conhecer as informações cadastrais do paciente (nome, CPF, data de nascimento, telefone, endereço e e-mail).
2. Fornecer identificação unívoca do paciente no sistema.

### Colaborações
* Consulta
* Internacao

---

**ProfissionalSaude**

### Responsabilidades
1. Conhecer os dados de identificação e qualificação do profissional (nome, registro profissional/CRM/COREN, especialidade, telefone e e-mail).
2. Identificar unicamente o profissional no corpo clínico do hospital.

### Colaborações
* Consulta

---

**Quarto**

### Responsabilidades
1. Conhecer os dados estruturais do quarto (número/identificador, capacidade máxima de leitos, ocupação atual e status).
2. Avaliar se possui leito vago disponível para nova internação.
3. Incrementar a ocupação atual quando uma internação for realizada.
4. Decrementar a ocupação atual quando um paciente receber alta médica.

### Colaborações
* StatusQuarto
* Internacao

---

**Consulta**

### Responsabilidades
1. Conhecer a data e o horário agendados para o atendimento médico.
2. Conhecer o paciente associado à consulta.
3. Conhecer o profissional de saúde responsável pelo atendimento.
4. Conhecer o status atual da consulta (Agendada, Realizada ou Cancelada) e observações médicas.

### Colaborações
* Paciente
* ProfissionalSaude
* StatusConsulta

---

**Internacao**

### Responsabilidades
1. Conhecer o paciente sob regime de internação.
2. Conhecer o quarto e leito alocado para o paciente.
3. Conhecer a data/hora de entrada, data/hora de alta médica e motivo da internação.
4. Conhecer e atualizar o status da internação (Em Andamento, Alta ou Cancelada).

### Colaborações
* Paciente
* Quarto
* StatusInternacao

---

**HistoricoMedico**

### Responsabilidades
1. Consolidar em um único prontuário todas as consultas realizadas e agendadas de um determinado paciente.
2. Consolidar todas as internações registradas no histórico do paciente.
3. Fornecer uma visão unificada da ficha médica e do paciente.

### Colaborações
* Paciente
* Consulta
* Internacao

---

**PacienteController**

### Responsabilidades
1. Receber e processar requisições HTTP REST relacionadas a pacientes (`/pacientes`).
2. Disparar a validação estrutural dos dados de entrada do paciente.
3. Delegar as operações de persistência e consulta para a camada de serviço de pacientes.
4. Retornar os códigos de status HTTP e os dados dos pacientes transferidos via DTO.

### Colaborações
* PacienteService
* PacienteDTO

---

**ProfissionalController**

### Responsabilidades
1. Receber e processar requisições HTTP REST relacionadas aos profissionais de saúde (`/profissionais`).
2. Disparar a validação dos dados de entrada do profissional.
3. Delegar as operações de cadastro e listagem para o serviço de profissionais.
4. Responder às requisições com os status HTTP adequados e DTOs estruturados.

### Colaborações
* ProfissionalSaudeService
* ProfissionalSaudeDTO
