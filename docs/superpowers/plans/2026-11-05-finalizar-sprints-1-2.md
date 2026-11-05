# Finalização das Sprints 1 e 2 - Plano de implementação

> **Para execução neste repositório:** aplicar TDD em cada tarefa, manter compatibilidade com o PostgreSQL/Supabase e criar um commit local descritivo após cada entrega verificável.

**Objetivo:** concluir as lacunas funcionais, REST, visuais e documentais das Sprints 1 e 2 sem incluir requisitos da Sprint 3.

**Arquitetura:** a hierarquia de profissionais usará uma tabela JPA `SINGLE_TABLE`; consultas e cirurgias compartilharão a `@MappedSuperclass Atendimento`, mas continuarão persistidas separadamente. Uma validação de agenda consultará os dois repositórios e aplicará a mesma regra de sobreposição `[início, fim)`.

**Stack:** Java 17, Spring Boot 3.2.3, Spring Data JPA, Bean Validation, PostgreSQL/Supabase, H2 para testes, HTML/CSS/JavaScript.

**Especificação:** `docs/superpowers/specs/2026-11-05-correcoes-sprints-1-2-design.md`

## Restrições globais

- Preservar dados, endpoints compatíveis, PostgreSQL/Supabase e estruturas legadas dependentes.
- Cancelar consultas e internações logicamente; não excluir seus registros.
- Não classificar profissionais existentes por inferência nem executar migração destrutiva.
- Não alterar a identidade Git, não fazer push e não desenvolver a Sprint 3.

---

### Tarefa 1: hierarquia JPA de profissionais

**Arquivos:** entidades `ProfissionalSaude`, `ClinicoGeral`, `Especialista`, `Cirurgiao`; DTO, repositório, serviço, controlador e testes correspondentes.

- [ ] Escrever testes que comprovem persistência polimórfica, categoria explícita, pesquisa por especialidade, atualização e exclusão protegida.
- [ ] Executar os testes e confirmar falha pela ausência da hierarquia/contratos.
- [ ] Implementar `@Inheritance(SINGLE_TABLE)`, discriminador `categoria`, subclasses e contratos REST.
- [ ] Executar testes focados e a suíte; criar commit `implementa hierarquia de profissionais`.

### Tarefa 2: atendimentos, cirurgia e conflitos de agenda

**Arquivos:** `Atendimento`, modalidades de consulta, `ProcedimentoCirurgico`, DTOs, repositórios, serviço de agenda e testes.

- [ ] Escrever testes de validação das quatro modalidades, cirurgião obrigatório, tipo, intervalos previstos/efetivos e sobreposição consulta-cirurgia.
- [ ] Confirmar falhas pelos campos/serviço ausentes.
- [ ] Implementar `Atendimento` como `@MappedSuperclass`, ampliar a cirurgia e centralizar a regra `existente.inicio < novo.fim && existente.fim > novo.inicio`.
- [ ] Executar testes focados e a suíte; criar commit `completa atendimentos e conflitos de agenda`.

### Tarefa 3: CRUDs, cancelamento lógico e histórico

**Arquivos:** DTOs, serviços, controladores e testes de pacientes, profissionais, consultas, internações, quartos e histórico.

- [ ] Escrever testes dos endpoints de atualização/remoção, filtros por situação e histórico com consultas, internações e cirurgias.
- [ ] Confirmar que `DELETE /consultas/{id}` e `DELETE /internacoes/{id}` ainda não existem.
- [ ] Implementar os contratos REST mantendo os CRUDs já completos e cancelamento lógico idempotente onde for seguro.
- [ ] Executar testes de controladores/integração e a suíte; criar commit `completa cruds e preserva historico`.

### Tarefa 4: seis telas funcionais e responsivas

**Arquivos:** `static/index.html`, `static/css/style.css`, `static/js/app.js` e testes HTTP/manuais no navegador.

- [ ] Definir os fluxos observáveis: listar, cadastrar, atualizar/cancelar e recarregar dados reais da API nas seis telas.
- [ ] Implementar formulários, tabelas/listas, estados vazio/erro/carregamento e navegação responsiva sem trocar o stack.
- [ ] Iniciar com H2, testar desktop e 390 px, conferir console/requisições e operações principais.
- [ ] Criar commit `integra telas com a api hospitalar`.

### Tarefa 5: banco e documentação acadêmica

**Arquivos:** SQL aditivo, diagrama textual/JPEG, CRCs, README, banco de dados, dúvidas e diagnóstico.

- [ ] Atualizar o SQL idempotente sem remover colunas ou dados.
- [ ] Atualizar diagrama e CRCs para refletirem as classes e contratos efetivos; gerar nova imagem do diagrama.
- [ ] Registrar no diagnóstico os resultados verificados e pendências que realmente dependem do grupo.
- [ ] Criar commit `atualiza documentacao das sprints 1 e 2`.

### Tarefa 6: verificação final

- [ ] Executar `mvn clean test` e conferir quantidade, falhas e erros.
- [ ] Iniciar a aplicação com H2, verificar `/`, as seis telas e os endpoints REST por requisições reais.
- [ ] Conferir `git diff`, `git status`, identidade e histórico local; não fazer push.
- [ ] Solicitar revisão técnica final e corrigir achados críticos/importantes antes da entrega.
