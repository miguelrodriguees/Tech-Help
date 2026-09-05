-- TechHelp V2 - Banco de dados MariaDB / MySQL Workbench


CREATE DATABASE IF NOT EXISTS tech_help
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE tech_help;

SET NAMES utf8mb4;

-- 1. Usuários e perfis


CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NULL,
    cpf CHAR(11) NOT NULL,
    foto_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_cpf UNIQUE (cpf),

    CONSTRAINT ck_usuario_status CHECK (
        status IN ('ATIVO', 'INATIVO', 'BLOQUEADO', 'EXCLUIDO')
    ),
    CONSTRAINT ck_usuario_cpf CHECK (
        cpf REGEXP '^[0-9]{11}$'
    )
) ENGINE=InnoDB;


CREATE TABLE perfil (
    id_perfil BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(30) NOT NULL,

    CONSTRAINT pk_perfil PRIMARY KEY (id_perfil),
    CONSTRAINT uk_perfil_nome UNIQUE (nome),

    CONSTRAINT ck_perfil_nome CHECK (
        nome IN ('CLIENTE', 'TECNICO', 'ADMIN')
    )
) ENGINE=InnoDB;


CREATE TABLE usuario_perfil (
    id_usuario BIGINT NOT NULL,
    id_perfil BIGINT NOT NULL,
    data_vinculo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_usuario_perfil PRIMARY KEY (id_usuario, id_perfil),

    CONSTRAINT fk_usuario_perfil_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_perfil_perfil
        FOREIGN KEY (id_perfil)
        REFERENCES perfil (id_perfil)
        ON DELETE RESTRICT
) ENGINE=InnoDB;


CREATE TABLE endereco (
    id_endereco BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    apelido VARCHAR(60) NULL,
    cep CHAR(8) NOT NULL,
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(120) NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_endereco PRIMARY KEY (id_endereco),

    CONSTRAINT fk_endereco_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT ck_endereco_cep CHECK (
        cep REGEXP '^[0-9]{8}$'
    ),
    CONSTRAINT ck_endereco_estado CHECK (
        estado REGEXP '^[A-Z]{2}$'
    )
) ENGINE=InnoDB;

CREATE INDEX idx_endereco_usuario
    ON endereco (id_usuario);

CREATE INDEX idx_endereco_localidade
    ON endereco (estado, cidade, bairro);


CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uk_cliente_usuario UNIQUE (id_usuario),

    CONSTRAINT fk_cliente_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
) ENGINE=InnoDB;


CREATE TABLE tecnico (
    id_tecnico BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    descricao TEXT NULL,
    anos_experiencia SMALLINT NOT NULL DEFAULT 0,
    status_verificacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tecnico PRIMARY KEY (id_tecnico),
    CONSTRAINT uk_tecnico_usuario UNIQUE (id_usuario),

    CONSTRAINT fk_tecnico_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT,

    CONSTRAINT ck_tecnico_anos_experiencia CHECK (
        anos_experiencia >= 0
    ),
    CONSTRAINT ck_tecnico_status_verificacao CHECK (
        status_verificacao IN ('PENDENTE', 'VERIFICADO', 'REJEITADO')
    )
) ENGINE=InnoDB;

CREATE INDEX idx_tecnico_verificacao
    ON tecnico (status_verificacao);


-- 2. Categorias e perfil profissional


CREATE TABLE categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nome UNIQUE (nome)
) ENGINE=InnoDB;


CREATE TABLE especialidade (
    id_especialidade BIGINT NOT NULL AUTO_INCREMENT,
    id_categoria BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_especialidade PRIMARY KEY (id_especialidade),
    CONSTRAINT uk_especialidade_categoria_nome
        UNIQUE (id_categoria, nome),

    CONSTRAINT fk_especialidade_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_especialidade_categoria_ativo
    ON especialidade (id_categoria, ativo);


CREATE TABLE tecnico_especialidade (
    id_tecnico BIGINT NOT NULL,
    id_especialidade BIGINT NOT NULL,
    data_vinculo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tecnico_especialidade
        PRIMARY KEY (id_tecnico, id_especialidade),

    CONSTRAINT fk_tecnico_especialidade_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE CASCADE,

    CONSTRAINT fk_tecnico_especialidade_especialidade
        FOREIGN KEY (id_especialidade)
        REFERENCES especialidade (id_especialidade)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_tecnico_especialidade_especialidade
    ON tecnico_especialidade (id_especialidade, id_tecnico);


CREATE TABLE portfolio (
    id_portfolio BIGINT NOT NULL AUTO_INCREMENT,
    id_tecnico BIGINT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT NULL,
    imagem_url VARCHAR(500) NULL,
    link_projeto VARCHAR(500) NULL,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_portfolio PRIMARY KEY (id_portfolio),

    CONSTRAINT fk_portfolio_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_portfolio_tecnico_data
    ON portfolio (id_tecnico, data_cadastro);


CREATE TABLE certificacao (
    id_certificacao BIGINT NOT NULL AUTO_INCREMENT,
    id_tecnico BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    instituicao VARCHAR(150) NOT NULL,
    codigo_credencial VARCHAR(120) NULL,
    arquivo_url VARCHAR(500) NULL,
    url_credencial VARCHAR(500) NULL,
    data_emissao DATE NULL,
    data_validade DATE NULL,
    status_verificacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_certificacao PRIMARY KEY (id_certificacao),

    CONSTRAINT fk_certificacao_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE CASCADE,

    CONSTRAINT uk_certificacao_tecnico_codigo
        UNIQUE (id_tecnico, codigo_credencial),

    CONSTRAINT ck_certificacao_status CHECK (
        status_verificacao IN ('PENDENTE', 'VERIFICADA', 'REJEITADA')
    ),
    CONSTRAINT ck_certificacao_datas CHECK (
        data_validade IS NULL
        OR data_emissao IS NULL
        OR data_validade >= data_emissao
    )
) ENGINE=InnoDB;

CREATE INDEX idx_certificacao_tecnico_status
    ON certificacao (id_tecnico, status_verificacao);


-- 3. Marketplace de serviços


CREATE TABLE solicitacao (
    id_solicitacao BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    id_endereco BIGINT NULL,

    titulo VARCHAR(160) NOT NULL,
    descricao TEXT NOT NULL,
    tipo_atendimento VARCHAR(20) NOT NULL,
    urgencia VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    orcamento_min DECIMAL(12,2) NULL,
    orcamento_max DECIMAL(12,2) NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'ABERTA',

    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_solicitacao PRIMARY KEY (id_solicitacao),

    CONSTRAINT fk_solicitacao_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT,

    CONSTRAINT fk_solicitacao_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT,

    CONSTRAINT fk_solicitacao_endereco
        FOREIGN KEY (id_endereco)
        REFERENCES endereco (id_endereco)
        ON DELETE SET NULL,

    CONSTRAINT ck_solicitacao_tipo_atendimento CHECK (
        tipo_atendimento IN ('PRESENCIAL', 'REMOTO', 'HIBRIDO')
    ),
    CONSTRAINT ck_solicitacao_urgencia CHECK (
        urgencia IN ('BAIXA', 'NORMAL', 'ALTA', 'EMERGENCIA')
    ),
    CONSTRAINT ck_solicitacao_status CHECK (
        status IN (
            'RASCUNHO',
            'ABERTA',
            'EM_NEGOCIACAO',
            'CONTRATADA',
            'CONCLUIDA',
            'CANCELADA'
        )
    ),
    CONSTRAINT ck_solicitacao_orcamento_min CHECK (
        orcamento_min IS NULL OR orcamento_min >= 0
    ),
    CONSTRAINT ck_solicitacao_orcamento_max CHECK (
        orcamento_max IS NULL OR orcamento_max >= 0
    ),
    CONSTRAINT ck_solicitacao_intervalo_orcamento CHECK (
        orcamento_min IS NULL
        OR orcamento_max IS NULL
        OR orcamento_max >= orcamento_min
    )
) ENGINE=InnoDB;

CREATE INDEX idx_solicitacao_cliente_status
    ON solicitacao (id_cliente, status, data_cadastro);

CREATE INDEX idx_solicitacao_categoria_status
    ON solicitacao (id_categoria, status, data_cadastro);

CREATE INDEX idx_solicitacao_atendimento_urgencia
    ON solicitacao (tipo_atendimento, urgencia);

CREATE FULLTEXT INDEX ft_solicitacao_busca
    ON solicitacao (titulo, descricao);


CREATE TABLE solicitacao_anexo (
    id_anexo BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitacao BIGINT NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    arquivo_url VARCHAR(500) NOT NULL,
    tipo_arquivo VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NULL,
    data_upload DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_solicitacao_anexo PRIMARY KEY (id_anexo),

    CONSTRAINT fk_solicitacao_anexo_solicitacao
        FOREIGN KEY (id_solicitacao)
        REFERENCES solicitacao (id_solicitacao)
        ON DELETE CASCADE,

    CONSTRAINT ck_solicitacao_anexo_tamanho CHECK (
        tamanho_bytes IS NULL OR tamanho_bytes >= 0
    )
) ENGINE=InnoDB;

CREATE INDEX idx_solicitacao_anexo_solicitacao
    ON solicitacao_anexo (id_solicitacao, data_upload);


CREATE TABLE proposta (
    id_proposta BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitacao BIGINT NOT NULL,
    id_tecnico BIGINT NOT NULL,

    valor DECIMAL(12,2) NOT NULL,
    mensagem TEXT NULL,
    prazo_estimado_dias SMALLINT NULL,
    data_disponivel DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENVIADA',

    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_proposta PRIMARY KEY (id_proposta),

    CONSTRAINT uk_proposta_solicitacao_tecnico
        UNIQUE (id_solicitacao, id_tecnico),

    CONSTRAINT fk_proposta_solicitacao
        FOREIGN KEY (id_solicitacao)
        REFERENCES solicitacao (id_solicitacao)
        ON DELETE RESTRICT,

    CONSTRAINT fk_proposta_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE RESTRICT,

    CONSTRAINT ck_proposta_valor CHECK (
        valor > 0
    ),
    CONSTRAINT ck_proposta_prazo CHECK (
        prazo_estimado_dias IS NULL OR prazo_estimado_dias >= 0
    ),
    CONSTRAINT ck_proposta_status CHECK (
        status IN (
            'ENVIADA',
            'ACEITA',
            'RECUSADA',
            'CANCELADA',
            'EXPIRADA'
        )
    )
) ENGINE=InnoDB;

CREATE INDEX idx_proposta_solicitacao_status
    ON proposta (id_solicitacao, status, data_cadastro);

CREATE INDEX idx_proposta_tecnico_status
    ON proposta (id_tecnico, status, data_cadastro);


-- 4. Conversas e mensagens


CREATE TABLE conversa (
    id_conversa BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitacao BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_conversa PRIMARY KEY (id_conversa),

    CONSTRAINT fk_conversa_solicitacao
        FOREIGN KEY (id_solicitacao)
        REFERENCES solicitacao (id_solicitacao)
        ON DELETE RESTRICT,

    CONSTRAINT ck_conversa_status CHECK (
        status IN ('ATIVA', 'ARQUIVADA', 'BLOQUEADA')
    )
) ENGINE=InnoDB;

CREATE INDEX idx_conversa_solicitacao
    ON conversa (id_solicitacao, status, data_atualizacao);


CREATE TABLE participante_conversa (
    id_conversa BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    data_entrada DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ultimo_acesso_em DATETIME(6) NULL,

    CONSTRAINT pk_participante_conversa
        PRIMARY KEY (id_conversa, id_usuario),

    CONSTRAINT fk_participante_conversa_conversa
        FOREIGN KEY (id_conversa)
        REFERENCES conversa (id_conversa)
        ON DELETE CASCADE,

    CONSTRAINT fk_participante_conversa_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_participante_conversa_usuario
    ON participante_conversa (id_usuario, id_conversa);


CREATE TABLE mensagem (
    id_mensagem BIGINT NOT NULL AUTO_INCREMENT,
    id_conversa BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'TEXTO',
    texto TEXT NULL,
    arquivo_url VARCHAR(500) NULL,
    data_envio DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_leitura DATETIME(6) NULL,

    CONSTRAINT pk_mensagem PRIMARY KEY (id_mensagem),

    CONSTRAINT fk_mensagem_conversa
        FOREIGN KEY (id_conversa)
        REFERENCES conversa (id_conversa)
        ON DELETE CASCADE,

    CONSTRAINT fk_mensagem_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT,

    CONSTRAINT ck_mensagem_tipo CHECK (
        tipo IN ('TEXTO', 'IMAGEM', 'ARQUIVO', 'SISTEMA')
    ),
    CONSTRAINT ck_mensagem_conteudo CHECK (
        texto IS NOT NULL OR arquivo_url IS NOT NULL
    )
) ENGINE=InnoDB;

CREATE INDEX idx_mensagem_conversa_data
    ON mensagem (id_conversa, data_envio);

CREATE INDEX idx_mensagem_usuario_data
    ON mensagem (id_usuario, data_envio);


-- 5. Serviços, avaliações e relacionamento


CREATE TABLE servico (
    id_servico BIGINT NOT NULL AUTO_INCREMENT,
    id_proposta BIGINT NOT NULL,
    id_cliente BIGINT NOT NULL,
    id_tecnico BIGINT NOT NULL,

    valor_acordado DECIMAL(12,2) NOT NULL,
    data_agendada DATETIME(6) NULL,
    data_inicio DATETIME(6) NULL,
    data_conclusao DATETIME(6) NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'AGENDADO',

    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    CONSTRAINT uk_servico_proposta UNIQUE (id_proposta),

    CONSTRAINT fk_servico_proposta
        FOREIGN KEY (id_proposta)
        REFERENCES proposta (id_proposta)
        ON DELETE RESTRICT,

    CONSTRAINT fk_servico_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT,

    CONSTRAINT fk_servico_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE RESTRICT,

    CONSTRAINT ck_servico_valor CHECK (
        valor_acordado > 0
    ),
    CONSTRAINT ck_servico_status CHECK (
        status IN ('AGENDADO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO')
    ),
    CONSTRAINT ck_servico_datas CHECK (
        data_conclusao IS NULL
        OR data_inicio IS NULL
        OR data_conclusao >= data_inicio
    )
) ENGINE=InnoDB;

CREATE INDEX idx_servico_cliente_status
    ON servico (id_cliente, status, data_cadastro);

CREATE INDEX idx_servico_tecnico_status
    ON servico (id_tecnico, status, data_cadastro);


CREATE TABLE avaliacao (
    id_avaliacao BIGINT NOT NULL AUTO_INCREMENT,
    id_servico BIGINT NOT NULL,
    id_avaliador BIGINT NOT NULL,
    id_avaliado BIGINT NOT NULL,

    nota TINYINT NOT NULL,
    comentario TEXT NULL,
    data_avaliacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_avaliacao PRIMARY KEY (id_avaliacao),

    CONSTRAINT uk_avaliacao_servico_partes
        UNIQUE (id_servico, id_avaliador, id_avaliado),

    CONSTRAINT fk_avaliacao_servico
        FOREIGN KEY (id_servico)
        REFERENCES servico (id_servico),

    CONSTRAINT fk_avaliacao_avaliador
        FOREIGN KEY (id_avaliador)
        REFERENCES usuario (id_usuario),

    CONSTRAINT fk_avaliacao_avaliado
        FOREIGN KEY (id_avaliado)
        REFERENCES usuario (id_usuario),

    CONSTRAINT ck_avaliacao_nota CHECK (
        nota BETWEEN 1 AND 5
    ),
    CONSTRAINT ck_avaliacao_usuarios_distintos CHECK (
        id_avaliador <> id_avaliado
    )
) ENGINE=InnoDB;

CREATE INDEX idx_avaliacao_avaliado_data
    ON avaliacao (id_avaliado, data_avaliacao);

CREATE INDEX idx_avaliacao_servico
    ON avaliacao (id_servico);


CREATE TABLE favorito (
    id_cliente BIGINT NOT NULL,
    id_tecnico BIGINT NOT NULL,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_favorito PRIMARY KEY (id_cliente, id_tecnico),

    CONSTRAINT fk_favorito_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorito_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_favorito_tecnico
    ON favorito (id_tecnico, id_cliente);


CREATE TABLE denuncia (
    id_denuncia BIGINT NOT NULL AUTO_INCREMENT,
    id_denunciante BIGINT NOT NULL,
    id_usuario_denunciado BIGINT NOT NULL,
    id_servico BIGINT NULL,

    motivo VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_resolucao DATETIME(6) NULL,
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_denuncia PRIMARY KEY (id_denuncia),

    CONSTRAINT fk_denuncia_denunciante
        FOREIGN KEY (id_denunciante)
        REFERENCES usuario (id_usuario),

    CONSTRAINT fk_denuncia_usuario_denunciado
        FOREIGN KEY (id_usuario_denunciado)
        REFERENCES usuario (id_usuario),

    CONSTRAINT fk_denuncia_servico
        FOREIGN KEY (id_servico)
        REFERENCES servico (id_servico),

    CONSTRAINT ck_denuncia_status CHECK (
        status IN ('ABERTA', 'EM_ANALISE', 'RESOLVIDA', 'ARQUIVADA')
    ),
    CONSTRAINT ck_denuncia_usuarios_distintos CHECK (
        id_denunciante <> id_usuario_denunciado
    )
) ENGINE=InnoDB;

CREATE INDEX idx_denuncia_status_data
    ON denuncia (status, data_cadastro);

CREATE INDEX idx_denuncia_usuario_denunciado
    ON denuncia (id_usuario_denunciado, status);


CREATE TABLE notificacao (
    id_notificacao BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(160) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    link_destino VARCHAR(500) NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_leitura DATETIME(6) NULL,

    CONSTRAINT pk_notificacao PRIMARY KEY (id_notificacao),

    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_notificacao_usuario_lida_data
    ON notificacao (id_usuario, lida, data_cadastro);


-- 6. Aluguel de ferramentas e kits


CREATE TABLE ferramenta (
    id_ferramenta BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(140) NOT NULL,
    descricao TEXT NULL,
    quantidade_total INT NOT NULL DEFAULT 0,
    quantidade_disponivel INT NOT NULL DEFAULT 0,
    valor_diaria DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_ferramenta PRIMARY KEY (id_ferramenta),
    CONSTRAINT uk_ferramenta_nome UNIQUE (nome),

    CONSTRAINT ck_ferramenta_quantidade_total CHECK (
        quantidade_total >= 0
    ),
    CONSTRAINT ck_ferramenta_quantidade_disponivel CHECK (
        quantidade_disponivel >= 0
        AND quantidade_disponivel <= quantidade_total
    ),
    CONSTRAINT ck_ferramenta_valor CHECK (
        valor_diaria >= 0
    ),
    CONSTRAINT ck_ferramenta_status CHECK (
        status IN ('DISPONIVEL', 'MANUTENCAO', 'INATIVA')
    )
) ENGINE=InnoDB;

CREATE INDEX idx_ferramenta_status_disponibilidade
    ON ferramenta (status, quantidade_disponivel);


CREATE TABLE kit (
    id_kit BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(140) NOT NULL,
    descricao TEXT NULL,
    valor_diaria DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_kit PRIMARY KEY (id_kit),
    CONSTRAINT uk_kit_nome UNIQUE (nome),

    CONSTRAINT ck_kit_valor CHECK (
        valor_diaria >= 0
    ),
    CONSTRAINT ck_kit_status CHECK (
        status IN ('DISPONIVEL', 'INDISPONIVEL', 'INATIVO')
    )
) ENGINE=InnoDB;


CREATE TABLE kit_ferramenta (
    id_kit BIGINT NOT NULL,
    id_ferramenta BIGINT NOT NULL,
    quantidade INT NOT NULL,

    CONSTRAINT pk_kit_ferramenta
        PRIMARY KEY (id_kit, id_ferramenta),

    CONSTRAINT fk_kit_ferramenta_kit
        FOREIGN KEY (id_kit)
        REFERENCES kit (id_kit)
        ON DELETE CASCADE,

    CONSTRAINT fk_kit_ferramenta_ferramenta
        FOREIGN KEY (id_ferramenta)
        REFERENCES ferramenta (id_ferramenta)
        ON DELETE RESTRICT,

    CONSTRAINT ck_kit_ferramenta_quantidade CHECK (
        quantidade > 0
    )
) ENGINE=InnoDB;

CREATE INDEX idx_kit_ferramenta_ferramenta
    ON kit_ferramenta (id_ferramenta, id_kit);


CREATE TABLE aluguel (
    id_aluguel BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,

    data_prevista_retirada DATETIME(6) NOT NULL,
    data_prevista_devolucao DATETIME(6) NOT NULL,
    data_retirada DATETIME(6) NULL,
    data_devolucao DATETIME(6) NULL,

    valor_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    valor_caucao DECIMAL(12,2) NOT NULL DEFAULT 0,
    valor_multa DECIMAL(12,2) NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'RESERVADO',
    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_aluguel PRIMARY KEY (id_aluguel),

    CONSTRAINT fk_aluguel_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON DELETE RESTRICT,

    CONSTRAINT ck_aluguel_periodo_previsto CHECK (
        data_prevista_devolucao > data_prevista_retirada
    ),
    CONSTRAINT ck_aluguel_periodo_real CHECK (
        data_devolucao IS NULL
        OR data_retirada IS NULL
        OR data_devolucao >= data_retirada
    ),
    CONSTRAINT ck_aluguel_valores CHECK (
        valor_total >= 0
        AND valor_caucao >= 0
        AND valor_multa >= 0
    ),
    CONSTRAINT ck_aluguel_status CHECK (
        status IN (
            'RESERVADO',
            'RETIRADO',
            'DEVOLVIDO',
            'ATRASADO',
            'CANCELADO'
        )
    )
) ENGINE=InnoDB;

CREATE INDEX idx_aluguel_usuario_status
    ON aluguel (id_usuario, status, data_cadastro);

CREATE INDEX idx_aluguel_status_devolucao
    ON aluguel (status, data_prevista_devolucao);


CREATE TABLE aluguel_item (
    id_aluguel_item BIGINT NOT NULL AUTO_INCREMENT,
    id_aluguel BIGINT NOT NULL,
    id_kit BIGINT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    valor_diaria DECIMAL(12,2) NOT NULL,

    CONSTRAINT pk_aluguel_item PRIMARY KEY (id_aluguel_item),

    CONSTRAINT uk_aluguel_item_aluguel_kit
        UNIQUE (id_aluguel, id_kit),

    CONSTRAINT fk_aluguel_item_aluguel
        FOREIGN KEY (id_aluguel)
        REFERENCES aluguel (id_aluguel)
        ON DELETE CASCADE,

    CONSTRAINT fk_aluguel_item_kit
        FOREIGN KEY (id_kit)
        REFERENCES kit (id_kit)
        ON DELETE RESTRICT,

    CONSTRAINT ck_aluguel_item_quantidade CHECK (
        quantidade > 0
    ),
    CONSTRAINT ck_aluguel_item_valor CHECK (
        valor_diaria >= 0
    )
) ENGINE=InnoDB;

CREATE INDEX idx_aluguel_item_kit
    ON aluguel_item (id_kit, id_aluguel);


-- 7. Pagamentos


CREATE TABLE pagamento (
    id_pagamento BIGINT NOT NULL AUTO_INCREMENT,

    -- Pagamento ligado a um serviço ou a um aluguel.
    id_servico BIGINT NULL,
    id_aluguel BIGINT NULL,

    valor DECIMAL(12,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',

    gateway VARCHAR(50) NULL,
    id_transacao_externa VARCHAR(150) NULL,

    data_cadastro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_pagamento DATETIME(6) NULL,
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_pagamento PRIMARY KEY (id_pagamento),
    CONSTRAINT uk_pagamento_transacao_externa UNIQUE (id_transacao_externa),

    CONSTRAINT fk_pagamento_servico
        FOREIGN KEY (id_servico)
        REFERENCES servico (id_servico),

    CONSTRAINT fk_pagamento_aluguel
        FOREIGN KEY (id_aluguel)
        REFERENCES aluguel (id_aluguel),

    CONSTRAINT ck_pagamento_valor CHECK (
        valor > 0
    ),
    CONSTRAINT ck_pagamento_forma CHECK (
        forma_pagamento IN (
            'PIX',
            'CARTAO_CREDITO',
            'CARTAO_DEBITO',
            'BOLETO',
            'DINHEIRO'
        )
    ),
    CONSTRAINT ck_pagamento_status CHECK (
        status IN (
            'PENDENTE',
            'PROCESSANDO',
            'PAGO',
            'FALHOU',
            'ESTORNADO',
            'CANCELADO'
        )
    ),
    CONSTRAINT ck_pagamento_origem CHECK (
        (id_servico IS NOT NULL AND id_aluguel IS NULL)
        OR
        (id_servico IS NULL AND id_aluguel IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE INDEX idx_pagamento_servico_status
    ON pagamento (id_servico, status, data_cadastro);

CREATE INDEX idx_pagamento_aluguel_status
    ON pagamento (id_aluguel, status, data_cadastro);

CREATE INDEX idx_pagamento_status_data
    ON pagamento (status, data_cadastro);


-- 8. Dados iniciais


INSERT INTO perfil (id_perfil, nome) VALUES
    (1, 'CLIENTE'),
    (2, 'TECNICO'),
    (3, 'ADMIN');

INSERT INTO categoria (id_categoria, nome, descricao, ativo) VALUES
    (1, 'Hardware', 'Manutenção, montagem, diagnóstico e componentes físicos.', TRUE),
    (2, 'Software', 'Sistemas operacionais, programas, instalação e configuração.', TRUE),
    (3, 'Redes', 'Wi-Fi, cabeamento, roteadores, switches e infraestrutura de rede.', TRUE),
    (4, 'Segurança da Informação', 'Proteção de dispositivos, contas e ambientes digitais.', TRUE),
    (5, 'Servidores e Cloud', 'Servidores, virtualização, Linux, serviços e nuvem.', TRUE),
    (6, 'Impressoras e Periféricos', 'Instalação, configuração e manutenção de periféricos.', TRUE);

INSERT INTO especialidade
    (id_categoria, nome, descricao, ativo)
VALUES
    (1, 'Montagem de Computadores', 'Montagem e configuração de computadores e workstations.', TRUE),
    (1, 'Manutenção de Computadores', 'Diagnóstico, limpeza, troca e reparo de componentes.', TRUE),
    (1, 'Upgrade de Hardware', 'Análise e atualização de peças e componentes.', TRUE),

    (2, 'Formatação e Sistemas Operacionais', 'Instalação e configuração de Windows ou Linux.', TRUE),
    (2, 'Instalação de Programas', 'Instalação, atualização e configuração de softwares.', TRUE),
    (2, 'Remoção de Malware', 'Diagnóstico e remoção de softwares maliciosos.', TRUE),

    (3, 'Configuração de Wi-Fi', 'Configuração e otimização de redes sem fio.', TRUE),
    (3, 'Cabeamento Estruturado', 'Instalação, organização e certificação básica de cabeamento.', TRUE),
    (3, 'Roteadores e Switches', 'Configuração de equipamentos e segmentação básica de rede.', TRUE),
    (3, 'MikroTik', 'Configuração e suporte a equipamentos MikroTik.', TRUE),

    (4, 'Segurança de Dispositivos', 'Hardening básico, antivírus e boas práticas de segurança.', TRUE),
    (4, 'Recuperação de Contas', 'Suporte a acesso e recuperação segura de contas.', TRUE),

    (5, 'Linux', 'Instalação, configuração e administração de ambientes Linux.', TRUE),
    (5, 'Servidores', 'Configuração e manutenção de serviços de servidor.', TRUE),
    (5, 'Cloud Computing', 'Configuração e suporte básico a serviços em nuvem.', TRUE),

    (6, 'Impressoras', 'Instalação, configuração e diagnóstico de impressoras.', TRUE),
    (6, 'Periféricos', 'Configuração e diagnóstico de dispositivos periféricos.', TRUE);


-- 9. Views


CREATE OR REPLACE VIEW vw_tecnico_resumo AS
SELECT
    t.id_tecnico,
    u.id_usuario,
    u.nome,
    u.foto_url,
    t.descricao,
    t.anos_experiencia,
    t.status_verificacao,
    COALESCE(ROUND(AVG(a.nota), 2), 0.00) AS avaliacao_media,
    COUNT(DISTINCT CASE
        WHEN s.status = 'CONCLUIDO' THEN s.id_servico
        ELSE NULL
    END) AS servicos_concluidos
FROM tecnico t
INNER JOIN usuario u
    ON u.id_usuario = t.id_usuario
LEFT JOIN servico s
    ON s.id_tecnico = t.id_tecnico
LEFT JOIN avaliacao a
    ON a.id_servico = s.id_servico
   AND a.id_avaliado = u.id_usuario
GROUP BY
    t.id_tecnico,
    u.id_usuario,
    u.nome,
    u.foto_url,
    t.descricao,
    t.anos_experiencia,
    t.status_verificacao;


CREATE OR REPLACE VIEW vw_solicitacao_resumo AS
SELECT
    s.id_solicitacao,
    s.id_cliente,
    s.id_categoria,
    c.nome AS categoria,
    s.titulo,
    s.tipo_atendimento,
    s.urgencia,
    s.orcamento_min,
    s.orcamento_max,
    s.status,
    s.data_cadastro,
    COUNT(p.id_proposta) AS quantidade_propostas
FROM solicitacao s
INNER JOIN categoria c
    ON c.id_categoria = s.id_categoria
LEFT JOIN proposta p
    ON p.id_solicitacao = s.id_solicitacao
GROUP BY
    s.id_solicitacao,
    s.id_cliente,
    s.id_categoria,
    c.nome,
    s.titulo,
    s.tipo_atendimento,
    s.urgencia,
    s.orcamento_min,
    s.orcamento_max,
    s.status,
    s.data_cadastro;


-- 10. Consultas de verificação


SELECT DATABASE() AS banco_atual;

SHOW FULL TABLES;

SELECT * FROM perfil ORDER BY id_perfil;
SELECT * FROM categoria ORDER BY id_categoria;
SELECT * FROM especialidade ORDER BY id_categoria, nome;
