-- =============================================================================
-- Sistema de Informacao Hospitalar - criacao do banco de dados (MySQL 8)
--
-- Execute UMA vez, conectado como root (MySQL Workbench ou terminal):
--     mysql -u root -p < docs/sql/01_criar_banco.sql
--
-- As tabelas NAO precisam ser criadas a mao: o Hibernate as cria na primeira
-- execucao da aplicacao (spring.jpa.hibernate.ddl-auto=update).
--
-- ATENCAO: usuario/senha abaixo sao apenas de DESENVOLVIMENTO LOCAL e coincidem
-- com os padroes do application.properties. Nunca reutilize em producao.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS hospitaldb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'hospital_app'@'localhost' IDENTIFIED BY 'hospital123';

GRANT ALL PRIVILEGES ON hospitaldb.* TO 'hospital_app'@'localhost';

FLUSH PRIVILEGES;

-- Conferencia (opcional):
-- SHOW DATABASES LIKE 'hospitaldb';
-- SHOW GRANTS FOR 'hospital_app'@'localhost';
