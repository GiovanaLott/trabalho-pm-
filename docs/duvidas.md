# Dúvidas e decisões pendentes

Este arquivo registra decisões que dependem do grupo. Enquanto não houver resposta, a implementação deve preservar os dados e as funcionalidades existentes e não deve criar regras por suposição.

## 1. Classificação dos profissionais já cadastrados

O banco PostgreSQL/Supabase pode conter profissionais criados antes da hierarquia `ClinicoGeral`, `Especialista` e `Cirurgiao`.

**Dúvida para o grupo:** quais registros existentes pertencem a cada categoria?

**Informação necessária:** para cada profissional existente, informar o identificador ou registro profissional e a categoria correta.

**Decisão provisória:** não classificar profissionais existentes com base no texto da especialidade, no formato do registro ou nos procedimentos vinculados. A migração da hierarquia somente será aplicada ao banco compartilhado depois de os registros serem identificados e classificados pelo grupo. Novos cadastros deverão informar explicitamente a categoria.

## 2. Exclusão de consultas e internações

O requisito geral solicita CRUD, mas o sistema também deve preservar o histórico médico do paciente.

**Dúvida para o grupo:** o professor espera exclusão física desses registros ou aceita cancelamento lógico como operação de remoção?

**Decisão provisória:** não excluir fisicamente consultas nem internações. Os endpoints de remoção usarão cancelamento lógico, manterão o registro no histórico e permitirão diferenciar registros ativos e cancelados nas consultas e listagens.

## 3. Tipos de quarto

O escopo fornecido define provisoriamente `ENFERMARIA`, `APARTAMENTO` e `UTI`. O código existente também possui `ISOLAMENTO`, com entidade, enum, persistência e testes próprios.

**Dúvida para o grupo:** o tipo `ISOLAMENTO` foi aprovado pelo professor ou deve ser retirado da entrega?

**Decisão provisória:** preservar `ISOLAMENTO` para não apagar uma funcionalidade existente e testada. Não acrescentar novos tipos até o grupo confirmar o conjunto definitivo.

## 4. Duração padrão das consultas

A regra de conflito deve considerar intervalos, mas o modelo atual registra apenas o horário inicial da consulta.

**Dúvida para o grupo:** existe uma duração padrão definida pelo professor ou cada consulta deve informar a própria duração?

**Decisão provisória:** adicionar `duracaoMinutos` ao atendimento e exigir valor positivo nos novos cadastros. Para manter compatibilidade com consultas antigas, a API usará 30 minutos quando o campo não for informado, repetindo a janela mínima já adotada pelo código atual.

## 5. Estruturas legadas de situação da consulta

O sistema possui `SituacaoConsulta`, que atende ao requisito atual, e `StatusConsulta`, mantido por compatibilidade com código anterior.

**Dúvida para o grupo:** existe algum consumidor externo ou banco antigo que ainda dependa de `StatusConsulta.REALIZADA`?

**Decisão provisória:** não remover `StatusConsulta` nem os métodos de compatibilidade até concluir a busca de dependências no código, nos testes e no esquema/migração do banco. Se não houver dependências externas confirmadas, a remoção será feita em commit separado.
