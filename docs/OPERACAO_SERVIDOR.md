# Instalação, reinício e atualização do Tech Help

## Estado e pré-requisitos

Preparação para um servidor Ubuntu com systemd, Java 25 e MariaDB. Não instalada nem testada em servidor real. Cadastro/hospedagem ainda pendentes. Confirmar comandos, arquitetura e versões na máquina escolhida antes de executar.

O serviço usa `/usr/bin/java`: confirmar `java -version` (25) e o caminho no servidor. Frontend e backend estão no mesmo JAR. MariaDB e Java devem ficar acessíveis somente localmente; endereço público e HTTPS serão tratados pelo proxy. Não abrir portas 3306 ou 8080 para visitantes.

O usuário `techhelp` é uma conta do sistema operacional que executa o Java, diferente das contas cliente/técnico/ADMIN do site e do usuário do banco. Não recebe sudo nem login interativo. As credenciais reais ficam em `/etc/techhelp/techhelp.env`, fora do repositório.

## Preparar na máquina Windows, pelo CMD

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
preparar-hospedagem.cmd
```

O script reutiliza `preparar-demonstracao.cmd`: gera React com API relativa `/`, empacota com Maven e imprime SHA-256 do JAR. O build não roda testes; os testes pertinentes são executados separadamente. `demonstracao` é o nome do perfil Maven de empacotamento. No servidor, ativar o perfil Spring `hospedagem`.

Quando tivermos servidor e chave SSH, substituir os caminhos e IP abaixo pelos dados reais. Os nomes são exemplos, não comandos prontos para o servidor atual:

```bat
scp -i "C:\CAMINHO\chave-do-servidor" "backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar" ubuntu@IP_DO_SERVIDOR:/tmp/techhelp-novo.jar
scp -i "C:\CAMINHO\chave-do-servidor" "deploy\linux\techhelp.service" "deploy\linux\techhelp.env.example" ubuntu@IP_DO_SERVIDOR:/tmp/
ssh -i "C:\CAMINHO\chave-do-servidor" ubuntu@IP_DO_SERVIDOR
```

Conferir a identidade/chave pública do servidor pelo painel do provedor antes de aceitar a primeira conexão SSH. Não distribuir a chave privada nem colocá-la no Git. Após entrar por SSH, os comandos abaixo são Linux: executar na sessão remota, não diretamente no CMD do Windows.

## Primeira instalação no Linux remoto

Antes desta etapa, banco e tabelas devem existir e o usuário da aplicação deve ter sido criado. Não importar os dados locais implicitamente nem executar o script inicial em bancos existentes. `ddl-auto=validate` não cria o esquema.

Verificar primeiro se já existe uma instalação para não sobrescrevê-la:

```sh
getent passwd techhelp
sudo ls -ld /opt/techhelp /etc/techhelp
sudo systemctl status techhelp --no-pager
```

Resultados de inexistência são esperados somente numa primeira instalação. Se existir conta, configuração ou serviço com esses nomes, parar e analisar antes de reutilizar. Depois de confirmar que é uma instalação nova:

```sh
sudo useradd --system --user-group --home-dir /opt/techhelp --no-create-home --shell /usr/sbin/nologin techhelp
sudo install -d -o root -g techhelp -m 0750 /opt/techhelp
sudo install -d -o root -g root -m 0700 /etc/techhelp
sha256sum /tmp/techhelp-novo.jar
sudo install -o root -g techhelp -m 0640 /tmp/techhelp-novo.jar /opt/techhelp/techhelp.jar
sudo install -o root -g root -m 0600 /tmp/techhelp.env.example /etc/techhelp/techhelp.env
sudo nano /etc/techhelp/techhelp.env
```

Comparar o hash com o mostrado no Windows. Preencher URL, usuário e senha válidos no editor privado do servidor. Não usar a senha fictícia do exemplo. O formato é `EnvironmentFile` do systemd: não adicionar `export` e não executar esse arquivo como script. Aspas e barras em senhas precisam seguir a sintaxe documentada do EnvironmentFile; não imprimir o arquivo ou a senha nos logs.

Com Java e MariaDB confirmados, instalar e conferir o serviço:

```sh
sudo install -o root -g root -m 0644 /tmp/techhelp.service /etc/systemd/system/techhelp.service
sudo systemd-analyze verify /etc/systemd/system/techhelp.service
sudo systemctl daemon-reload
sudo systemctl enable --now techhelp
sudo systemctl status techhelp --no-pager
sudo journalctl -u techhelp -n 80 --no-pager
```

O serviço inicia junto com o servidor e tenta reiniciar após falha, com limite de cinco partidas em cinco minutos. Falhas permanentes de configuração exigem correção; não há repetição ilimitada. Antes de compartilhar logs, verificar se contêm dados privados.

Conferir a aplicação dentro do próprio servidor, aguardando a inicialização terminar:

```sh
curl --fail --silent --show-error http://127.0.0.1:8080/categorias
curl --silent --output /dev/null --write-out '%{http_code}\n' http://127.0.0.1:8080/auth/me
```

Categorias deve responder com sucesso; `/auth/me` deve retornar 401 sem login. Essa consulta HTTP é somente em leitura: cookies Secure exigem HTTPS para autenticar. Após configurar proxy/certificado, conferir login pelo endereço HTTPS. Adaptar a porta se TECHHELP_PORT tiver sido alterada.

## Atualizar uma instalação existente

Este procedimento vale para atualização do JAR sem mudanças de esquema. Fazer backup próprio do banco antes de atualizações que alterem dados. Nesta versão não há migração automática. A substituição reinicia o Java, interrompe o atendimento por alguns instantes e encerra as sessões em memória; usuários precisarão entrar novamente.

Gerar novo pacote no Windows e transferir para `/tmp/techhelp-novo.jar`. No Linux remoto, comparar SHA-256 e garantir que não há outra atualização sendo feita ao mesmo tempo:

```sh
sha256sum /tmp/techhelp-novo.jar
sudo install -o root -g techhelp -m 0640 /tmp/techhelp-novo.jar /opt/techhelp/techhelp.next.jar
sudo cp -p /opt/techhelp/techhelp.jar /opt/techhelp/techhelp.previous.jar
sudo mv /opt/techhelp/techhelp.next.jar /opt/techhelp/techhelp.jar
sudo systemctl restart techhelp
sudo systemctl status techhelp --no-pager
sudo journalctl -u techhelp -n 80 --no-pager
```

Executar cada comando apenas se o anterior tiver sucesso. A cópia preserva somente a versão imediatamente anterior; o rename no mesmo diretório troca o arquivo completo. Repetir as consultas de leitura e conferir o HTTPS. Se precisar modificar o arquivo `.service`, validar e executar daemon-reload antes de reiniciar. Para mudança apenas no arquivo de variáveis, basta reiniciar o serviço.

## Voltar ao JAR anterior

Se a atualização falhar, conservar o JAR anterior e restaurar por cópia, sem apagá-lo:

```sh
sudo install -o root -g techhelp -m 0640 /opt/techhelp/techhelp.previous.jar /opt/techhelp/techhelp.rollback.jar
sudo mv /opt/techhelp/techhelp.rollback.jar /opt/techhelp/techhelp.jar
sudo systemctl reset-failed techhelp
sudo systemctl restart techhelp
sudo systemctl status techhelp --no-pager
```

Conferir novamente as consultas e o HTTPS. **Voltar o JAR não desfaz registros gravados, alterações de tabelas ou mudanças nas variáveis.** A configuração de banco e os backups são tratados separadamente. A versão antiga precisa continuar compatível com o banco.

## Limites da preparação

Modelo do serviço revisado localmente; inicialização após reboot, reinício por falha, permissões Linux, proxy HTTPS, conectividade MariaDB remota e retorno à versão anterior dependem de validação no servidor real. Não apresentar estes passos como já executados. Não houve compra de domínio, instalação remota ou transferência de dados pessoais.

Referências para a futura conferência do sistema: https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html e https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html .
