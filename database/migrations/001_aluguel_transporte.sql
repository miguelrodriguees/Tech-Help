-- Execute UMA VEZ no banco selecionado. Pare no primeiro erro.
-- Não apaga dados. Backend deve estar parado durante a aplicação.
-- Bloqueia migração com reservas legadas ativas: o estoque precisa de conciliação.
CREATE TEMPORARY TABLE guarda_aluguel_legado (
 ativos INT NOT NULL CHECK (ativos = 0)
);
INSERT INTO guarda_aluguel_legado
 SELECT COUNT(*) FROM aluguel WHERE status IN ('RESERVADO','RETIRADO','ATRASADO');
DROP TEMPORARY TABLE guarda_aluguel_legado;
ALTER TABLE aluguel
  ADD COLUMN tipo_recebimento VARCHAR(20) NOT NULL DEFAULT 'RETIRADA',
  ADD COLUMN tipo_devolucao VARCHAR(20) NOT NULL DEFAULT 'NO_TECHHELP',
  ADD COLUMN endereco_transporte VARCHAR(600) NULL,
  ADD COLUMN taxa_entrega DECIMAL(12,2) NULL,
  ADD COLUMN taxa_coleta DECIMAL(12,2) NULL,
  ADD COLUMN observacao_transporte VARCHAR(1000) NULL,
  ADD COLUMN chave_pedido VARCHAR(36) NULL,
  ADD COLUMN pedido_hash CHAR(64) NULL,
  ADD COLUMN estoque_reservado BOOLEAN NOT NULL DEFAULT FALSE,
  ADD CONSTRAINT uk_aluguel_chave UNIQUE (id_usuario, chave_pedido),
  ADD CONSTRAINT ck_aluguel_taxas CHECK ((taxa_entrega IS NULL OR taxa_entrega >= 0) AND (taxa_coleta IS NULL OR taxa_coleta >= 0)),
  ADD CONSTRAINT ck_aluguel_recebimento CHECK (tipo_recebimento IN ('RETIRADA','ENTREGA')),
  ADD CONSTRAINT ck_aluguel_devolucao CHECK (tipo_devolucao IN ('NO_TECHHELP','COLETA'));
ALTER TABLE aluguel DROP CONSTRAINT ck_aluguel_status;
ALTER TABLE aluguel ADD CONSTRAINT ck_aluguel_status CHECK (
 status IN ('AGUARDANDO_TAXA','AGUARDANDO_ACEITE','RESERVADO','RETIRADO','DEVOLVIDO','ATRASADO','CANCELADO')
);
ALTER TABLE aluguel_item
  MODIFY COLUMN id_kit BIGINT NULL,
  ADD COLUMN id_ferramenta BIGINT NULL,
  ADD CONSTRAINT uk_aluguel_item_ferramenta UNIQUE (id_aluguel,id_ferramenta),
  ADD CONSTRAINT fk_aluguel_item_ferramenta FOREIGN KEY (id_ferramenta) REFERENCES ferramenta(id_ferramenta),
  ADD CONSTRAINT ck_aluguel_item_tipo CHECK ((id_kit IS NOT NULL AND id_ferramenta IS NULL) OR (id_kit IS NULL AND id_ferramenta IS NOT NULL));
-- Composição congelada do pedido: devolver repõe exatamente o que foi reservado.
CREATE TABLE aluguel_consumo (
 id_aluguel BIGINT NOT NULL,
 id_ferramenta BIGINT NOT NULL,
 quantidade INT NOT NULL,
 PRIMARY KEY(id_aluguel,id_ferramenta),
 FOREIGN KEY(id_aluguel) REFERENCES aluguel(id_aluguel),
 FOREIGN KEY(id_ferramenta) REFERENCES ferramenta(id_ferramenta),
 CHECK(quantidade > 0)
) ENGINE=InnoDB;
