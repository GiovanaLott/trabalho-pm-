-- =============================================================================
-- Ajuste do banco PostgreSQL (Supabase) para o modelo da Sprint 2.
--
-- Por que é necessário: o Hibernate está com ddl-auto=update, que CRIA colunas
-- novas mas não consegue (1) preencher colunas obrigatórias em tabelas que já têm
-- linhas, (2) atualizar as restrições CHECK que ele mesmo gera para os enums nem
-- (3) renomear colunas. Sem este ajuste, um banco criado nas versões anteriores
-- falha ao gravar quartos (coluna "tipo"), consultas (coluna "modalidade" e a troca
-- de "status" por "situacao") e ao colocar um quarto em manutenção.
--
-- Banco sem dados importantes? É mais simples apagar as tabelas e deixar o
-- Hibernate recriá-las na próxima execução:
--   DROP TABLE IF EXISTS procedimentos_cirurgicos, internacoes, consultas,
--                        quartos, pacientes, profissionais_saude CASCADE;
--
-- O script é idempotente: pode ser executado mais de uma vez sem efeitos colaterais.
-- Executar no SQL Editor do Supabase.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- QUARTOS: tipos de quarto (herança), atributos específicos e controle de versão
-- -----------------------------------------------------------------------------
ALTER TABLE quartos ADD COLUMN IF NOT EXISTS tipo VARCHAR(20);

-- Quartos antigos não tinham tipo: classifica pela capacidade (revise depois se necessário).
UPDATE quartos
   SET tipo = CASE
                WHEN capacidade_maxima = 1 THEN 'APARTAMENTO'
                WHEN capacidade_maxima BETWEEN 2 AND 8 THEN 'ENFERMARIA'
                ELSE 'UTI'
              END
 WHERE tipo IS NULL;
ALTER TABLE quartos ALTER COLUMN tipo SET NOT NULL;

ALTER TABLE quartos ADD COLUMN IF NOT EXISTS permite_acompanhante BOOLEAN;
UPDATE quartos SET permite_acompanhante = TRUE WHERE tipo = 'APARTAMENTO' AND permite_acompanhante IS NULL;

ALTER TABLE quartos ADD COLUMN IF NOT EXISTS tipo_isolamento VARCHAR(20);

ALTER TABLE quartos ADD COLUMN IF NOT EXISTS versao BIGINT;
UPDATE quartos SET versao = 0 WHERE versao IS NULL;

-- O enum StatusQuarto ganhou EM_MANUTENCAO: remove o CHECK antigo gerado pelo Hibernate.
ALTER TABLE quartos DROP CONSTRAINT IF EXISTS quartos_situacao_check;
ALTER TABLE quartos DROP CONSTRAINT IF EXISTS quartos_tipo_check;

-- -----------------------------------------------------------------------------
-- CONSULTAS: modalidades (herança) e troca de "status" por "situacao"
-- -----------------------------------------------------------------------------
ALTER TABLE consultas ADD COLUMN IF NOT EXISTS modalidade VARCHAR(31);
UPDATE consultas SET modalidade = 'ROTINA' WHERE modalidade IS NULL;
ALTER TABLE consultas ALTER COLUMN modalidade SET NOT NULL;

ALTER TABLE consultas ADD COLUMN IF NOT EXISTS situacao VARCHAR(30);
ALTER TABLE consultas ADD COLUMN IF NOT EXISTS especialidade_requerida VARCHAR(80);
ALTER TABLE consultas ADD COLUMN IF NOT EXISTS prioridade_emergencia VARCHAR(20);

DO $$
BEGIN
    -- Copia a coluna antiga "status" para "situacao" (REALIZADA passou a se chamar CONCLUIDA)
    -- e deixa de exigi-la, já que a aplicação não a preenche mais.
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_name = 'consultas' AND column_name = 'status') THEN
        UPDATE consultas
           SET situacao = CASE status WHEN 'REALIZADA' THEN 'CONCLUIDA' ELSE status END
         WHERE situacao IS NULL;
        ALTER TABLE consultas ALTER COLUMN status DROP NOT NULL;
    END IF;
END $$;

UPDATE consultas SET situacao = 'AGENDADA' WHERE situacao IS NULL;
ALTER TABLE consultas ALTER COLUMN situacao SET NOT NULL;
ALTER TABLE consultas DROP CONSTRAINT IF EXISTS consultas_status_check;
ALTER TABLE consultas DROP CONSTRAINT IF EXISTS consultas_situacao_check;
ALTER TABLE consultas DROP CONSTRAINT IF EXISTS consultas_modalidade_check;
