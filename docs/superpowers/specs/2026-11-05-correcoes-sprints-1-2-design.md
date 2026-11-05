# Correções das Sprints 1 e 2 - Especificação de projeto

## Objetivo

Completar os requisitos das Sprints 1 e 2 do sistema hospitalar sem desenvolver a Sprint 3, preservando PostgreSQL/Supabase, os dados existentes, os endpoints compatíveis e as funcionalidades que já passam nos testes.

## Restrições globais

- Manter Java 17, Spring Boot 3.2.3, Maven, JPA e PostgreSQL/Supabase.
- Usar H2 somente no perfil local e nos testes.
- Não fazer push nem alterar a identidade ou a autenticação do Git.
- Implementar em etapas pequenas, orientadas por testes e com um commit local por etapa.
- Não classificar profissionais existentes por inferência.
- Não excluir fisicamente consultas ou internações enquanto o grupo não decidir sobre a preservação do histórico.
- Não remover estruturas legadas antes de verificar dependências no código, nos testes e no banco.
- Não desenvolver requisitos da Sprint 3.

## Estratégia escolhida

A implementação será incremental. O modelo atual continuará usando as tabelas separadas de consultas e procedimentos cirúrgicos. Uma classe-base JPA mapeada fornecerá os dados e comportamentos comuns dos atendimentos, enquanto as entidades concretas manterão seus endpoints e repositórios atuais.

As hierarquias usarão herança `SINGLE_TABLE` quando os objetos compartilham a mesma tabela e `@MappedSuperclass` quando compartilham código, mas permanecem em tabelas diferentes. Scripts SQL serão apenas aditivos e idempotentes. Nenhuma migração destrutiva será executada automaticamente.

## Etapa 1 - Registro das decisões pendentes

O arquivo `docs/duvidas.md` registra:

- a classificação manual dos profissionais existentes;
- cancelamento lógico versus exclusão física;
- a permanência do tipo de quarto `ISOLAMENTO`;
- a duração padrão das consultas antigas;
- a dependência da estrutura legada `StatusConsulta`.

Essas dúvidas não impedem as mudanças compatíveis, mas bloqueiam qualquer conversão irreversível de dados.

## Etapa 2 - Hierarquia de profissionais

`ProfissionalSaude` concentrará nome, registro, telefone e e-mail. `ClinicoGeral`, `Especialista` e `Cirurgiao` representarão as categorias exigidas. `Especialista` armazenará a especialidade e `Cirurgiao` será o único tipo aceito em procedimentos cirúrgicos.

Novos cadastros informarão explicitamente a categoria. A API devolverá a categoria em seus DTOs e terá pesquisa de especialistas por especialidade. Atualização e exclusão serão adicionadas; a exclusão física de profissional somente será permitida quando não houver consultas, internações ou cirurgias vinculadas.

Antes de modificar a tabela compartilhada, será verificado se existem credenciais disponíveis para consultar os profissionais atuais sem expor segredos. Se os registros não puderem ser consultados ou o grupo não fornecer a classificação, a migração de produção permanecerá documentada e não será executada. Os testes usarão dados explicitamente categorizados.

## Etapa 3 - Base comum dos atendimentos

Uma classe abstrata `Atendimento` será introduzida como `@MappedSuperclass`. Ela reunirá identificador, paciente, profissional, início previsto, duração e o contrato `validarCondicoesAtendimento()`.

`Consulta` continuará sendo a raiz `SINGLE_TABLE` de `ConsultaRotina`, `ConsultaEspecializada` e `AtendimentoEmergencia`. `ProcedimentoCirurgico` continuará em sua tabela, mas herdará os dados comuns de `Atendimento` e acrescentará tipo, observações pré/pós-operatórias, início e fim efetivos e situação cirúrgica.

A compatibilidade JSON será preservada quando possível. Campos antigos de data e horário de cirurgia serão aceitos durante a transição e convertidos para o novo intervalo previsto, sem apagar colunas ou dados automaticamente.

## Etapa 4 - Conflitos por intervalo

Dois atendimentos do mesmo profissional entrarão em conflito quando seus intervalos se sobrepuserem. Para um intervalo `[inicio, fim)`, há sobreposição quando:

`inicioExistente < fimNovo` e `fimExistente > inicioNovo`.

Consultas usarão o início previsto e `duracaoMinutos`. Cirurgias usarão início e fim previstos enquanto estiverem agendadas; quando houver horários efetivos, esses dados também serão considerados nas validações operacionais e no histórico.

O serviço consultará tanto consultas quanto procedimentos cirúrgicos ativos. Registros cancelados não bloquearão horários. Os testes cobrirão limites adjacentes, sobreposição parcial, inclusão total, conflito entre consulta e cirurgia e ausência de conflito com cancelados.

## Etapa 5 - Cancelamento lógico e listagens

`DELETE /consultas/{id}` e `DELETE /internacoes/{id}` executarão cancelamento lógico e não removerão linhas. O comportamento respeitará as transições já existentes e preservará o histórico.

As listagens continuarão podendo mostrar todos os registros, sempre com a situação. Também oferecerão filtro explícito para ativos/cancelados, sem alterar silenciosamente o resultado dos endpoints atuais. Endpoints específicos de registros ativos continuarão disponíveis.

## Etapa 6 - Compatibilidade e estruturas legadas

Antes de remover `StatusConsulta`, construtores antigos ou métodos de compatibilidade, serão executadas buscas em `src/main`, `src/test`, documentação e scripts SQL. Também será verificado se o esquema compartilhado ainda possui a coluna antiga `status`.

Se houver dependência não resolvida, a estrutura será mantida e marcada como compatibilidade. Remoções seguras ocorrerão em commit próprio, com testes que comprovem a ausência de regressão.

`HistoricoMedico`, seu DTO e seu serviço serão alinhados para representar consultas, internações e procedimentos cirúrgicos de forma consistente.

## Etapa 7 - Artefatos acadêmicos

Após estabilizar o código:

- atualizar `docs/DIAGRAMA_CLASSES.md` com classes, atributos, métodos, heranças, multiplicidades e enums reais;
- exportar novamente `docs/Diagrama_classes.jpeg` a partir do diagrama atualizado;
- criar `docs/crc.md` e alinhar `docs/CARTOES_CRC.md` com todas as classes do domínio;
- manter o PDF antigo como referência até o grupo transferir o conteúdo atualizado para o modelo do professor;
- atualizar `README.md`, `docs/BANCO_DE_DADOS.md` e o script SQL quando necessário;
- atualizar `docs/diagnostico.md` com o estado final verificado.

## Tratamento de erros

As novas violações usarão `RegraNegocioException`, `ChoqueHorarioException` ou `RecursoNaoEncontradoException`, preservando os códigos HTTP já definidos pelo `GlobalExceptionHandler`. Erros de categoria profissional, intervalo inválido, duração não positiva e conflito de agenda terão mensagens objetivas em português.

## Estratégia de testes

Cada comportamento novo seguirá o ciclo teste falhando, implementação mínima e teste passando. Serão priorizados testes de domínio e serviço; controladores terão testes dos novos contratos HTTP, e os fluxos principais terão cobertura de integração com H2.

Ao final de cada etapa serão executados os testes diretamente relacionados e a suíte completa quando a mudança afetar persistência ou contratos compartilhados. A revisão final executará `mvn test` e exigirá zero falhas antes da atualização final do diagnóstico.

## Critérios de conclusão

- Todas as lacunas das Sprints 1 e 2 estarão implementadas ou registradas como dependentes de decisão real do grupo.
- Nenhum profissional existente terá categoria atribuída por suposição.
- Somente `Cirurgiao` poderá ser associado a procedimento cirúrgico.
- Conflitos considerarão intervalos de consultas e cirurgias ativas.
- Cancelamentos preservarão o histórico e serão distinguíveis nas listagens.
- PostgreSQL/Supabase continuará sendo o banco principal.
- A documentação corresponderá ao código verificado.
- `mvn test` passará integralmente.
- Nenhuma funcionalidade da Sprint 3 será criada.
