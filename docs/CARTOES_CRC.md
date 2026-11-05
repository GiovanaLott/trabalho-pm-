# Cartões CRC (Class-Responsibility-Collaboration)

Trabalho Prático de Programação Modular — Sistema de Informação Hospitalar  
Curso de Engenharia de Software — PUC Minas

---

**Paciente**

### Responsabilidades
1. Conhecer as informações cadastrais do paciente (nome, CPF, data de nascimento, telefone, endereço e e-mail).
2. Fornecer identificação unívoca do paciente no sistema (CPF único).
3. Atualizar seus dados cadastrais.
4. Calcular a própria idade e informar se é menor de idade.

### Colaborações
* Cpf
* Consulta
* Internacao
* ProcedimentoCirurgico

---

**Cpf**

### Responsabilidades
1. Validar um CPF pelos dígitos verificadores, rejeitando sequências repetidas e tamanhos incorretos.
2. Padronizar o CPF no formato 000.000.000-00, aceitando entrada com ou sem máscara.

### Colaborações
* PacienteService

---

**ProfissionalSaude**

### Responsabilidades
1. Conhecer os dados de identificação e qualificação do profissional (nome, registro profissional/CRM/COREN, especialidade, telefone e e-mail).
2. Identificar unicamente o profissional no corpo clínico do hospital.

### Colaborações
* Consulta

---

**Quarto** *(abstrata)*

### Responsabilidades
1. Conhecer os dados estruturais do quarto (número de identificação, andar, capacidade máxima, ocupação atual e situação).
2. Conhecer o seu tipo (definido por cada subclasse).
3. Validar a própria configuração, incluindo a faixa de leitos permitida para o tipo.
4. Registrar a admissão de uma internação: verificar manutenção, vagas e requisitos do tipo, e ocupar um leito sem ultrapassar a capacidade máxima.
5. Liberar um leito na alta ou no cancelamento da internação.
6. Manter a situação atualizada (disponível/ocupado) e controlar a entrada e saída de manutenção (somente quando vazio).
7. Impedir a redução da capacidade abaixo da ocupação atual.

### Colaborações
* TipoQuarto
* StatusQuarto
* Internacao

---

**Enfermaria**

### Responsabilidades
1. Representar quartos coletivos, com 2 a 8 leitos.

### Colaborações
* Quarto
* TipoQuarto

---

**Apartamento**

### Responsabilidades
1. Representar quartos privativos, com 1 a 2 leitos.
2. Informar se permite acompanhante.

### Colaborações
* Quarto
* TipoQuarto

---

**UnidadeTerapiaIntensiva**

### Responsabilidades
1. Representar leitos de terapia intensiva (1 a 10 leitos).
2. Exigir justificativa clínica nas observações para aceitar uma internação.

### Colaborações
* Quarto
* Internacao

---

**QuartoIsolamento**

### Responsabilidades
1. Representar quartos individuais de isolamento (exatamente 1 leito).
2. Conhecer e exigir o tipo de precaução (contato, gotículas ou aerossóis).

### Colaborações
* Quarto
* TipoIsolamento

---

**TipoQuarto**

### Responsabilidades
1. Enumerar os tipos de quarto e conhecer a faixa de leitos permitida para cada um.
2. Criar a subclasse de quarto correspondente ao tipo (fábrica).

### Colaborações
* Enfermaria
* Apartamento
* UnidadeTerapiaIntensiva
* QuartoIsolamento

---

**Consulta**

### Responsabilidades
1. Conhecer a data e o horário agendados para o atendimento médico.
2. Conhecer o paciente associado à consulta.
3. Conhecer o profissional de saúde responsável pelo atendimento.
4. Conhecer a situação atual da consulta (Agendada, Confirmada, Em atendimento, Concluída, Cancelada ou Não compareceu).
5. Gerenciar e validar as transições de situação impedindo alterações incompatíveis.
6. Fornecer contrato de validação das condições necessárias para a realização do atendimento.

### Colaborações
* Paciente
* ProfissionalSaude
* SituacaoConsulta
* ModalidadeAtendimento

---

**ConsultaRotina**

### Responsabilidades
1. Representar consultas ambulatoriais de avaliação geral do paciente.
2. Validar que o motivo da consulta foi devidamente preenchido.
3. Permitir o registro de observações após a realização da consulta.

### Colaborações
* Consulta
* Paciente
* ProfissionalSaude

---

**ConsultaEspecializada**

### Responsabilidades
1. Representar consultas direcionadas a especialidades médicas específicas.
2. Armazenar a especialidade requerida para o atendimento.
3. Validar se a especialidade do profissional responsável é compatível com a especialidade solicitada.

### Colaborações
* Consulta
* ProfissionalSaude

---

**AtendimentoEmergencia**

### Responsabilidades
1. Representar atendimentos imediatos de pronto-socorro e urgência.
2. Armazenar e validar a classificação de prioridade (Baixa, Moderada, Alta, Crítica).
3. Identificar se o atendimento está aguardando avaliação, em atendimento ou já concluído.

### Colaborações
* Consulta
* PrioridadeAtendimento

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
3. Consolidar os procedimentos cirúrgicos do paciente.
4. Fornecer uma visão unificada da ficha médica e do paciente.

### Colaborações
* Paciente
* Consulta
* Internacao
* ProcedimentoCirurgico

---

**PacienteController**

### Responsabilidades
1. Receber e processar requisições HTTP REST relacionadas a pacientes (`/pacientes`): cadastro, listagem (com filtro por nome), busca por ID e por CPF, atualização e exclusão.
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

---

**ConsultaController**

### Responsabilidades
1. Receber requisições REST para agendamento, consulta e transição de estados de consultas (`/consultas`).
2. Expor endpoints de confirmação, início de atendimento, conclusão, cancelamento e não comparecimento.
3. Permitir filtragem de consultas por situação, modalidade e emergências pendentes.

### Colaborações
* ConsultaService
* ConsultaDTO
* ConsultaRequestDTO

---

**PacienteService**

### Responsabilidades
1. Validar e padronizar o CPF e garantir que ele seja único (no cadastro e na atualização).
2. Validar a data de nascimento (no passado e com idade plausível).
3. Cadastrar, atualizar, listar e buscar pacientes.
4. Impedir a exclusão de pacientes com consultas, internações ou cirurgias, preservando o histórico.

### Colaborações
* Paciente
* Cpf
* PacienteRepository
* ConsultaRepository
* InternacaoRepository
* ProcedimentoCirurgicoRepository

---

**QuartoController**

### Responsabilidades
1. Receber requisições REST relacionadas a quartos (`/quartos`): cadastro, listagem com filtros (tipo, andar, situação), quartos disponíveis, painel de ocupação, atualização, manutenção e exclusão.
2. Disparar a validação estrutural dos dados de entrada.
3. Delegar as operações ao serviço de quartos e responder com os status HTTP adequados.

### Colaborações
* QuartoService
* QuartoDTO
* QuartoUpdateDTO
* OcupacaoHospitalarDTO

---

**QuartoService**

### Responsabilidades
1. Criar o quarto do tipo solicitado e garantir número de identificação único.
2. Listar e filtrar quartos e calcular o painel de ocupação (geral e por tipo).
3. Atualizar dados do quarto respeitando a ocupação atual e a faixa do tipo.
4. Colocar e retirar quartos de manutenção.
5. Impedir a exclusão de quartos que já tiveram internações.

### Colaborações
* Quarto
* TipoQuarto
* QuartoRepository
* InternacaoRepository
