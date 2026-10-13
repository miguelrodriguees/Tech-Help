# Backup local do MariaDB

## Cópias conferidas em 11/10/2026

Backup antigo localizado em `C:\Users\Miguel Rodrigues\Desktop\backup_mariadb_completo.sql`, 1.570.347 bytes, concluído em 22/09/2026 às 23:42. O arquivo contém os bancos tech_help e tech_help_teste; essa identificação e o marcador final foram conferidos, mas o arquivo antigo não foi restaurado nesta etapa. Foi preservado sem alterações.

Novas cópias em `.local-backups/2026-10-11_232018/`:

| Banco | Arquivo | Tamanho | Verificação |
| --- | --- | --- | --- |
| tech_help | tech_help.sql | 50.226 bytes | Exportação e restauração isolada aprovadas |
| tech_help_teste | tech_help_teste.sql | 52.658 bytes | Exportação concluída; restauração não testada |

Exportação com mysqldump do XAMPP, single-transaction/quick, UTF-8, incluindo definições de rotinas, eventos e gatilhos. Saída gravada diretamente pelo programa, evitando redirecionamento com codificação alterada pelo shell. O banco principal contém tabelas InnoDB e não tinha rotinas, eventos ou gatilhos na verificação.

Restauração de tech_help feita em um schema temporário novo com nome aleatório. O dump não contém CREATE DATABASE ou USE; na cópia de validação, referências qualificadas das views foram direcionadas ao schema temporário. Criadas 28 tabelas e 2 views; quantidade de registros de cada tabela comparada com a origem, sem divergências. Schema temporário removido ao final. O dump original não foi modificado e os bancos existentes não foram sobrescritos.

Essa conferência comprova importação e contagens, não uma comparação de cada campo ou o funcionamento inteiro da aplicação contra a cópia restaurada. O arquivo manifesto.json dentro da pasta local registra hashes SHA-256 e contagens, permitindo conferir se os arquivos mudaram depois.

## Onde preservar

A pasta `.local-backups/` está excluída do Git: os dumps contêm dados pessoais e hashes de senha. Commit e push não incluem esses arquivos. Esta cópia está no mesmo computador; para preservá-la fora dele, copiar a pasta completa, incluindo manifesto.json, para um dispositivo externo ou local privado de backup escolhido pelo usuário. Não enviar os dumps a um repositório público.

## Nova exportação pelo CMD

Com MariaDB ativo, criar uma pasta nova para cada cópia. Substituir DATA_HORA por uma identificação única; não reutilizar o nome de uma cópia existente.

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
mkdir ".local-backups\DATA_HORA"
"C:\xampp\mysql\bin\mysqldump.exe" -u root --single-transaction --quick --routines --events --triggers --hex-blob --default-character-set=utf8mb4 --result-file=".local-backups\DATA_HORA\tech_help.sql" tech_help
if errorlevel 1 echo A exportacao falhou. Confira o erro antes de usar o arquivo.
```

A conta root sem senha corresponde ao ambiente local atual. Se a configuração mudar, ajustar a conta e usar `-p` para digitar a senha no terminal, sem colocá-la no comando salvo.

Uma restauração exige conferir o destino e os comandos do dump antes da importação. O backup completo antigo contém instruções para bancos existentes; não importá-lo sobre o servidor atual como teste de integridade. Usar um destino isolado e preservar os bancos atuais.
