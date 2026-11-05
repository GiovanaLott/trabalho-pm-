# Diagnóstico das Sprints 1 e 2

Diagnóstico realizado em 05/11/2026, antes de qualquer alteração no código-fonte.

## Escopo da análise

Foram revisados:

- a estrutura completa do repositório;
- os documentos, o diagrama em Markdown, a imagem do diagrama, os cartões CRC em Markdown e o PDF disponível em `docs/`;
- as entidades, DTOs, repositórios, serviços, controladores e tratamento de exceções;
- a interface estática em HTML, CSS e JavaScript;
- os testes unitários, de controladores, repositórios e integração;
- a configuração de persistência e o histórico recente do Git.

Os enunciados originais das Sprints 1 e 2 não estão versionados em `docs/`. A pasta contém artefatos produzidos pelo grupo, documentação do banco e um script SQL. Por isso, este diagnóstico usa como referência os requisitos fornecidos para esta revisão e compara esses requisitos com o estado atual do repositório.

## Resultado por requisito

| Requisito | Arquivo/localização | Status | O que precisa ser feito |
|---|---|---|---|
| Sprint 1 - tela de pacientes | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática possui formulário e listagem coerentes com o domínio. |
| Sprint 1 - tela de profissionais | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática de cadastro e listagem está presente. |
| Sprint 1 - tela de consultas | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática de agendamento e listagem está presente. |
| Sprint 1 - tela de internações | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática de registro e histórico está presente. |
| Sprint 1 - tela de quartos | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática de cadastro e visualização de quartos está presente. |
| Sprint 1 - tela de histórico médico | `src/main/resources/static/index.html`, `static/css/style.css` | OK | A tela estática de prontuário está presente. |
| Consistência visual das telas | `src/main/resources/static/index.html`, `static/css/style.css`, `static/js/app.js` | OK | As seis telas compartilham navegação, componentes e estilos. O JavaScript apenas controla a navegação e interações conceituais. |
| Diagrama de classes em formato editável | `docs/DIAGRAMA_CLASSES.md` | PARCIAL | O Mermaid e o PlantUML representam pacientes, consultas, internações e a hierarquia de quartos, mas não representam `ProcedimentoCirurgico`, `HistoricoMedico`, a hierarquia exigida de profissionais, seus relacionamentos e enums relacionados. Atualizar somente depois de o modelo de código ser corrigido. |
| Imagem exportada do diagrama | `docs/Diagrama_classes.jpeg` | PARCIAL | A imagem existe, mas está desatualizada: mostra apenas o modelo antigo, usa `StatusConsulta` e não contém as heranças e modalidades já presentes no código. Reexportar a imagem após atualizar o diagrama. |
| Cartões CRC em Markdown | `docs/CARTOES_CRC.md` | PARCIAL | Há cartões para várias classes atuais, porém o arquivo solicitado `docs/crc.md` não existe e faltam classes importantes do domínio, como `ProcedimentoCirurgico`, as categorias de profissionais e vários enums. Adequar o nome/organização e completar os cartões após definir o modelo final. |
| Cartões CRC no modelo em PDF | `docs/Cartões CRC - Sistema Hospitalar (Versão Final).pdf` | PARCIAL | O PDF está legível, mas contém apenas o modelo antigo com cinco classes e três enums. Não acompanha o Markdown nem o código atual. Atualizar o conteúdo transferido para o modelo do professor. |
| Arquitetura em camadas | `controladores/`, `servicos/`, `repositorios/`, `entidades/` e `dtos/` | OK | A separação entre Controller, Service, Repository e Model está implementada e é usada pela aplicação. |
| Tratamento de exceções e respostas HTTP | `src/main/java/com/hospital/excecoes/` | OK | Há tratamento global e respostas para validação, recurso inexistente, conflito e erro inesperado. Manter o padrão ao criar os endpoints que faltam. |
| Dados comuns dos profissionais | `entidades/ProfissionalSaude.java` | OK | Nome, registro, telefone e e-mail existem. A especialidade também está no tipo base atual. |
| Hierarquia de profissionais: clínico geral, especialista e cirurgião | `entidades/ProfissionalSaude.java` | FALTANDO | `ProfissionalSaude` é uma classe concreta única. Criar a hierarquia exigida sem duplicar os dados comuns. |
| Especialista com pesquisa por especialidade | `ProfissionalSaudeRepository.java`, `ProfissionalSaudeService.java`, `ProfissionalController.java` | FALTANDO | Não há subclasse `Especialista`, método de repositório, serviço ou endpoint de pesquisa por especialidade. |
| Cirurgião com procedimentos realizados | `entidades/ProcedimentoCirurgico.java`, `entidades/ProfissionalSaude.java` | PARCIAL | O procedimento referencia um `ProfissionalSaude` genérico como cirurgião. Não existe a categoria `Cirurgiao` nem a modelagem específica dos procedimentos realizados por ela. |
| Classe base comum para os quatro atendimentos | `entidades/Consulta.java`, `entidades/ProcedimentoCirurgico.java` | PARCIAL | `Consulta` é base de rotina, especializada e emergência, mas o procedimento cirúrgico é uma entidade separada sem uma classe-base comum às quatro modalidades. |
| Consulta de rotina com validação própria | `entidades/ConsultaRotina.java` | OK | A modalidade existe e valida o preenchimento do motivo. |
| Consulta especializada compatível com a área do profissional | `entidades/ConsultaEspecializada.java` | OK | A modalidade exige especialidade e compara com a especialidade do profissional. A validação deverá ser adaptada à futura hierarquia de profissionais. |
| Atendimento de emergência com prioridade e situação | `entidades/AtendimentoEmergencia.java`, `PrioridadeAtendimento.java` | PARCIAL | As quatro prioridades existem. A situação é reaproveitada de `SituacaoConsulta` e “aguardando” é inferido de `AGENDADA`/`CONFIRMADA`, em vez de possuir claramente os estados aguardando, iniciado e concluído previstos para emergência. |
| Procedimento cirúrgico com dados e ciclo de vida completos | `entidades/ProcedimentoCirurgico.java`, `ProcedimentoCirurgicoService.java` | PARCIAL | Descrição, observações pré/pós, paciente, profissional e situações existem; a regra de não concluir sem iniciar funciona. Faltam o tipo do procedimento, a distinção completa entre datas/horários previstos e efetivos e a garantia de que o profissional seja um cirurgião. |
| Polimorfismo nas validações das modalidades | `Consulta.java`, `ConsultaRotina.java`, `ConsultaEspecializada.java`, `AtendimentoEmergencia.java` | PARCIAL | Há validação polimórfica para três modalidades de consulta. A cirurgia não participa do mesmo contrato. |
| Enum de situação das consultas | `entidades/SituacaoConsulta.java` | OK | Os seis valores exigidos estão presentes e são persistidos como enum. Existe também o legado redundante `StatusConsulta`, que deve ser removido ou justificado depois de verificar compatibilidade. |
| Validação das transições de consulta | `entidades/Consulta.java`, `ConsultaServiceTest.java` | OK | O código impede retorno a agendada, conclusão sem atendimento e alteração de consultas concluídas, canceladas ou com não comparecimento. O `setSituacao` público ainda permite contornar as regras fora do fluxo normal e deve ser revisto. |
| Tipos provisórios de quarto | `entidades/TipoQuarto.java` | PARCIAL | `ENFERMARIA`, `APARTAMENTO` e `UTI` existem, mas foi acrescentado `ISOLAMENTO` com regras não previstas no escopo fornecido. A decisão não foi registrada em `docs/duvidas.md`. Confirmar com o grupo antes de manter ou remover o tipo adicional. |
| Registro de dúvidas e suposições | `docs/duvidas.md` | FALTANDO | O arquivo não existe. Registrar pelo menos a suposição dos tipos de quarto e qualquer decisão relevante antes da implementação. |
| Impedir dois atendimentos do profissional no mesmo horário | `ConsultaService.java`, `ConsultaRepository.java`, `ProcedimentoCirurgicoService.java` | PARCIAL | Consultas, inclusive suas três subclasses, verificam conflito em uma janela de 30 minutos. Cirurgias não verificam conflito com outras cirurgias nem com consultas; portanto a regra não cobre todos os atendimentos. |
| Internação exige paciente e quarto disponível | `InternacaoService.java`, `Quarto.java` | OK | O serviço exige paciente, profissional e quarto existentes, e o quarto valida disponibilidade antes da gravação. |
| Quarto não ultrapassa capacidade máxima | `Quarto.java`, `InternacaoService.java` | OK | A entidade impede ocupação acima da capacidade e há controle de versão para concorrência otimista. |
| Histórico de consultas e internações do paciente | `HistoricoMedicoService.java`, `HistoricoMedicoDTO.java` | OK | O endpoint consolida consultas e internações e também inclui procedimentos cirúrgicos. A classe de domínio `HistoricoMedico` não possui a lista de cirurgias e deve ser alinhada ao DTO/serviço. |
| CRUD REST de pacientes | `PacienteController.java`, `PacienteService.java` | OK | Existem cadastro, listagem/filtro, busca, atualização e exclusão protegida por histórico. |
| CRUD REST de profissionais | `ProfissionalController.java`, `ProfissionalSaudeService.java` | PARCIAL | Existem cadastro, listagem e busca por ID. Faltam atualização, exclusão e pesquisa por especialidade. |
| CRUD REST de consultas | `ConsultaController.java`, `ConsultaService.java` | PARCIAL | Existem criação, leituras, filtros e transições de situação. Não há atualização dos dados do agendamento nem exclusão, apesar da exigência de CRUD. |
| CRUD REST de internações | `InternacaoController.java`, `InternacaoService.java` | PARCIAL | Existem criação, leituras, atualização limitada, alta e cancelamento. Não há exclusão. Confirmar se o grupo deseja preservar histórico por cancelamento em vez de exclusão e registrar a decisão. |
| CRUD REST de quartos | `QuartoController.java`, `QuartoService.java` | OK | Existem criação, leituras/filtros, atualização, exclusão e operações de manutenção. |
| Consulta do histórico de um paciente | `HistoricoMedicoController.java`, `HistoricoMedicoService.java` | OK | O endpoint `GET /historico/paciente/{pacienteId}` está implementado. |
| Persistência escolhida pelo grupo | `application.properties`, `application-h2.properties`, `docs/BANCO_DE_DADOS.md` | OK | PostgreSQL hospedado no Supabase é a escolha registrada. H2 é usado apenas nos testes e no perfil local alternativo. A escolha deve ser preservada. |
| Teste de conflito de horário | `ConsultaServiceTest.java`, `HospitalApiIntegrationTest.java` | PARCIAL | Há casos com e sem conflito para consultas. Faltam casos envolvendo procedimentos cirúrgicos e conflito entre modalidades diferentes. |
| Teste de capacidade máxima do quarto | `QuartoTest.java`, `InternacaoServiceTest.java`, `HospitalApiIntegrationTest.java` | OK | Há casos válidos e inválidos, incluindo tentativa de internação em quarto lotado. |
| Teste de transições de situação da consulta | `ConsultaServiceTest.java` | OK | Há ciclo válido e tentativas inválidas de conclusão/alteração. |
| Teste de concluir cirurgia sem início | `ProcedimentoCirurgicoServiceTest.java` | OK | Há caso inválido sem início e caso válido após iniciar. |
| Build e suíte automatizada atuais | `pom.xml`, `src/test/` | OK | `mvn test` concluiu com sucesso: 167 testes executados, sem falhas, erros ou testes ignorados. |
| Ausência de Sprint 3 | repositório completo | OK | Não foi identificada implementação declarada como Sprint 3, e nenhuma funcionalidade dessa sprint foi criada nesta etapa. |

## Principais problemas encontrados

1. **A hierarquia de profissionais exigida não existe.** O sistema usa uma única classe concreta `ProfissionalSaude`, inclusive para cirurgias. Isso impede representar corretamente clínico geral, especialista e cirurgião e também impede a pesquisa específica por especialidade.
2. **Não há uma abstração comum para as quatro modalidades de atendimento.** O polimorfismo atual cobre somente rotina, especializada e emergência; cirurgia permanece separada.
3. **O procedimento cirúrgico está incompleto.** Faltam tipo, informações previstas/efetivas completas e vínculo obrigatório com um profissional da categoria cirurgião.
4. **A regra de conflito de horário é incompleta.** Ela funciona entre consultas, mas não considera cirurgias nem conflitos entre consulta e cirurgia.
5. **Os endpoints de profissionais, consultas e internações não entregam CRUD completo.** Profissionais também não podem ser pesquisados por especialidade.
6. **Os artefatos acadêmicos não estão sincronizados.** O JPEG do diagrama e o PDF dos CRCs representam uma versão antiga; o diagrama textual e o CRC em Markdown também omitem partes relevantes do domínio atual.
7. **`docs/duvidas.md` não existe.** A escolha provisória dos tipos de quarto não foi registrada, e o código acrescentou `ISOLAMENTO`, além dos três tipos definidos para esta revisão.
8. **Há inconsistências internas menores.** `HistoricoMedico` não inclui cirurgias, embora o DTO e o serviço incluam; `StatusConsulta` convive com `SituacaoConsulta`; e setters públicos permitem alterar diretamente estados que deveriam passar pelas regras de transição.
9. **Os enunciados originais não estão na pasta `docs/`.** Para manter rastreabilidade acadêmica, os arquivos originais ou links oficiais devem ser adicionados pelo grupo, se puderem ser versionados.

## Verificações executadas

- Identidade Git local conferida e preservada: Sofia de Abreu Botelho Romualdo.
- Árvore de trabalho estava limpa antes do diagnóstico.
- Diagrama JPEG e as três páginas do PDF de cartões CRC foram inspecionados visualmente.
- Comando executado: `mvn test`.
- Resultado: **BUILD SUCCESS**, 167 testes, 0 falhas, 0 erros e 0 ignorados.
- A compilação emite apenas um aviso de uso de API obsoleta ligado à compatibilidade com `StatusConsulta`.

## Ordem sugerida para a Etapa 2

Após autorização do grupo, a sequência mais segura é:

1. criar `docs/duvidas.md` e registrar as decisões pendentes;
2. implementar a hierarquia de profissionais e a pesquisa por especialidade;
3. alinhar as quatro modalidades sob uma base comum e completar o procedimento cirúrgico;
4. ampliar a regra de conflito de horário para todos os atendimentos;
5. completar os endpoints REST faltantes e seus testes;
6. alinhar `HistoricoMedico` e remover ou justificar estruturas legadas;
7. atualizar o diagrama, exportar a nova imagem e completar os cartões CRC/arquivo solicitado;
8. executar novamente a build e toda a suíte de testes.

Nenhuma alteração de implementação foi realizada nesta etapa.
