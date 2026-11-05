# Banco de Dados e Persistência

Trabalho Prático de Programação Modular — Sistema de Informação Hospitalar
Curso de Engenharia de Software — PUC Minas

---

## 1. Tecnologia escolhida: PostgreSQL (hospedado no Supabase)

O enunciado permite um banco relacional (MySQL, PostgreSQL) ou de documentos (MongoDB).
O grupo escolheu **PostgreSQL**, hospedado no **Supabase**, pelos motivos abaixo.

| Critério | Justificativa |
|---|---|
| **Natureza dos dados** | O domínio é fortemente relacional: consultas e internações ligam paciente, profissional e quarto, e o histórico é obtido cruzando essas tabelas. Um modelo de documentos exigiria duplicar dados (ex.: o paciente dentro de cada consulta) ou fazer as junções na aplicação. |
| **Integridade** | Chaves estrangeiras, `UNIQUE` (CPF, registro profissional, número do quarto) e `NOT NULL` são garantidos pelo próprio banco, como segunda barreira além das validações da aplicação (regra de negócio 7). |
| **Transações ACID** | Internar um paciente altera duas tabelas (`internacoes` e a ocupação em `quartos`). As duas alterações acontecem na mesma transação: ou ambas são gravadas, ou nenhuma. |
| **Concorrência** | Com o controle de versão otimista do JPA (`@Version` em `Quarto`), duas internações simultâneas no último leito não ultrapassam a capacidade (regra 5): a segunda falha e a API responde `409 Conflict`. |
| **Herança** | As hierarquias `Consulta` (rotina, especializada, emergência) e `Quarto` (enfermaria, apartamento, UTI, isolamento) são mapeadas com `SINGLE_TABLE`, que o JPA/Hibernate suporta nativamente em bancos relacionais. |
| **Supabase** | PostgreSQL gerenciado e gratuito, acessível de qualquer máquina do grupo, sem instalar servidor local; painel web para consultar os dados e executar SQL. |

## 2. Perfis de execução

| Perfil | Banco | Quando usar |
|---|---|---|
| *(padrão)* | PostgreSQL no Supabase | Execução normal da aplicação (`mvn spring-boot:run`) |
| `h2` | H2 em memória (zerado a cada execução) | Rodar a aplicação localmente sem acesso ao Supabase: `mvn spring-boot:run -Dspring-boot.run.profiles=h2` (console em `/h2-console`) |
| testes | H2 em memória | Automático em `mvn test` (`src/test/resources/application.properties`) |

### Variáveis de ambiente do perfil padrão

| Variável | Uso |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL (padrão: projeto Supabase do grupo) |
| `DB_USERNAME` | Usuário (padrão: `postgres`) |
| `DB_PASSWORD` | Senha do banco no Supabase (**obrigatória**; não deve ser escrita no código) |

## 3. Esquema

No perfil PostgreSQL, o Hibernate apenas valida o esquema (`spring.jpa.hibernate.ddl-auto=validate`).
As alterações são feitas pelo script versionado para impedir mudanças automáticas no banco compartilhado.

| Tabela | Observações |
|---|---|
| `pacientes` | `cpf` único, sempre gravado no formato `000.000.000-00` |
| `profissionais_saude` | `registro_profissional` único |
| `quartos` | Herança `SINGLE_TABLE`; a coluna `tipo` indica a subclasse (`ENFERMARIA`, `APARTAMENTO`, `UTI`, `ISOLAMENTO`). Colunas específicas: `permite_acompanhante` (apartamento) e `tipo_isolamento` (isolamento). `versao` é o controle de concorrência. |
| `consultas` | Herança `SINGLE_TABLE`; a coluna `modalidade` indica a subclasse (`ROTINA`, `ESPECIALIZADA`, `EMERGENCIA`) |
| `internacoes` | FKs para paciente, profissional e quarto |
| `procedimentos_cirurgicos` | FKs para paciente e cirurgião |

### Preservação do histórico (regra 6)

Pacientes e quartos que já aparecem em consultas, internações ou cirurgias **não podem ser excluídos**:
a API responde `400` com a explicação. Para retirar um quarto de uso, ele deve ser colocado em manutenção.

## 4. Atualizando um banco criado em versões anteriores

Um banco do Supabase criado antes desta sprint precisa do script
[`docs/sql/ajuste_banco_sprint2.sql`](sql/ajuste_banco_sprint2.sql) (executar no SQL Editor do Supabase).
O script é idempotente e não apaga registros. Antes de iniciar a aplicação, classifique manualmente
todos os profissionais legados listados pelo próprio script; o código não deduz categorias clínicas.
