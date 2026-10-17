-- Apply once to an existing PostgreSQL database before deploying the new entity model.
-- Preserve city and state values before removing the old locality tables.
ALTER TABLE endereco ADD COLUMN cidade varchar(80);
ALTER TABLE endereco ADD COLUMN estado varchar(2);

UPDATE endereco e
SET cidade = m.nome,
    estado = s.sigla
FROM municipio m
JOIN estado s ON s.id = m.id_estado
WHERE e.id_municipio = m.id;

ALTER TABLE endereco ALTER COLUMN cidade SET NOT NULL;
ALTER TABLE endereco ALTER COLUMN estado SET NOT NULL;
ALTER TABLE endereco DROP COLUMN id_municipio CASCADE;

DROP TABLE IF EXISTS paciente CASCADE;
DROP TABLE IF EXISTS psicologo CASCADE;
DROP TABLE IF EXISTS municipio CASCADE;
DROP TABLE IF EXISTS estado CASCADE;
