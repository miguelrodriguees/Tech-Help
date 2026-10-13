# Publicação do Tech Help em servidor remoto

## Estado em 12/10/2026

O usuário quer aprender a publicar o projeto em uma hospedagem independente do computador local. Mantida a preferência por R$ 0. Existe um pacote integrado de frontend e backend para demonstração, mas não existe servidor remoto provisionado nem publicação permanente concluída.

Caminho recomendado para tentativa: Oracle Cloud Always Free, usando uma máquina virtual Linux com Java 25 e MariaDB instalado nela. É uma recomendação condicionada ao cadastro e à disponibilidade, não uma garantia de hospedagem gratuita ou de disponibilidade contínua.

## Pesquisa de opções

Fontes oficiais consultadas em 12/10/2026:

- Oracle: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
- Cadastro e cartão: https://www.oracle.com/cloud/free/faq/
- Render: https://render.com/docs/free e https://render.com/docs/disks
- Koyeb: https://www.koyeb.com/docs/reference/instances

A documentação Oracle informa franquia A1 equivalente a 2 OCPUs e 12 GB de memória no total, além de armazenamento Always Free dentro dos limites. Conferir a franquia efetiva no painel antes de criar recursos. Preferir Ubuntu compatível com ARM para uma instância A1; a distribuição Java deve corresponder à arquitetura. Instâncias gratuitas podem estar sem capacidade e instâncias ociosas podem ser retomadas pelo provedor. Não atualizar a conta para paga como solução automática.

O cadastro Oracle exige cartão aceito para verificação e pode gerar autorização temporária. Não enviar dados de cartão, senha ou códigos de autenticação ao agente; preenchimento pelo próprio usuário no site oficial. Usar recursos Always Free, sem depender dos créditos de avaliação de 30 dias.

Render gratuito não oferece disco persistente para nosso MariaDB. Koyeb também não oferece volumes na instância gratuita da aplicação. Essas alternativas não foram escolhidas para o projeto completo. Não substituir MariaDB por PostgreSQL só para aproveitar uma oferta gratuita.

## Configuração preparada: perfil hospedagem

`backend/src/main/resources/application-hospedagem.properties` separa o servidor do ambiente local. É ativado explicitamente por `--spring.profiles.active=hospedagem`; não ativar junto com `demonstracao`, pois os perfis têm finalidades diferentes.

Variáveis obrigatórias, sem valores de desenvolvimento como alternativa:

| Variável | Finalidade |
| --- | --- |
| `TECHHELP_DB_URL` | URL JDBC MariaDB com servidor, porta e banco remoto |
| `TECHHELP_DB_USERNAME` | Usuário próprio da aplicação no banco remoto |
| `TECHHELP_DB_PASSWORD` | Senha desse usuário; fornecer valor não vazio |
| `TECHHELP_PORT` | Opcional: porta local do Java; padrão 8080 |

Sem uma variável obrigatória, o placeholder correspondente não pode ser resolvido; isso deve impedir que a aplicação use acidentalmente o banco/usuário locais. Definir uma variável vazia não equivale a deixá-la ausente: o perfil exige presença, mas não implementa validação adicional de conteúdo. Usar credenciais válidas e senha não vazia na configuração do servidor.

O perfil mantém `ddl-auto=validate`, desliga impressão de SQL e usa cookies Secure/HttpOnly/SameSite=Lax. O Java escuta somente em `127.0.0.1`: o proxy local será responsável pelo endereço público e HTTPS. Ele deve remover os cabeçalhos Forwarded/X-Forwarded-* recebidos do visitante e definir os próprios. A configuração dos cabeçalhos não é uma lista de proxies confiáveis; não expor a porta Java diretamente.

Exemplo **didático** no CMD, com credenciais fictícias, não executar sobre banco vazio ou indisponível esperando que as tabelas sejam criadas:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
set "TECHHELP_DB_URL=jdbc:mariadb://127.0.0.1:3306/tech_help_remoto"
set "TECHHELP_DB_USERNAME=techhelp_app"
set "TECHHELP_DB_PASSWORD=SENHA_FICTICIA_SUBSTITUIR_NO_SERVIDOR"
java -jar backend\target\techhelp-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=hospedagem
```

O JAR precisa ser reempacotado depois de adicionar este perfil para incluí-lo. O script `preparar-demonstracao.cmd` já empacota frontend e backend juntos; o nome do perfil Maven `demonstracao` controla o empacotamento, enquanto o perfil Spring `hospedagem` controla a execução. São mecanismos distintos.

No Linux remoto usaremos configuração privada do serviço, com acesso restrito, em vez desses comandos CMD. Não guardar valores reais no Git, em scripts compartilhados, documentação ou capturas. Ainda não foi criado arquivo com segredos nem definido servidor/domínio/proxy.

Os testes `HospedagemConfiguracaoTests` carregam os arquivos reais de configuração sem iniciar JPA/MariaDB: conferem variáveis externas, proteção HTTPS, ausência das três variáveis e preservação do perfil local. Não equivalem a validação de conexão remota, certificado, login ou serviço Linux.

## Execução automática e atualização preparadas

Modelos em `deploy/linux/techhelp.service` e `deploy/linux/techhelp.env.example`. O serviço executa Java como usuário Linux `techhelp`, sem privilégios administrativos, e lê as variáveis de arquivo privado do servidor. Reinicia após falhas com limite de tentativas. Ainda precisa ser instalado, validado com `systemd-analyze verify` e testado na máquina remota.

`preparar-hospedagem.cmd` reutiliza o empacotamento existente e mostra SHA-256 para comparar o pacote antes/depois da transferência. Instruções em [OPERACAO_SERVIDOR.md](OPERACAO_SERVIDOR.md): instalação, consulta de logs, atualização com cópia do JAR anterior e retorno ao pacote anterior. Reiniciar encerra as sessões em memória. Retornar o JAR não desfaz mudanças de dados ou esquema.

Nenhum arquivo com senha real foi criado; `deploy/linux/*.env` está excluído do Git, mas o exemplo permanece versionável. Nenhuma chave SSH, backup ou registro local foi transferido.

Verificação local desta preparação em 12/10/2026: preparar-hospedagem.cmd executado com sucesso, frontend compilado e JAR integrado gerado com SHA-256. Primeira tentativa bloqueada por spawn EPERM no ambiente; execução liberada passou. Testes foram pulados pelo script, sem nova suíte. Conferir instalação, reboot e atualização somente no Linux remoto quando houver servidor disponível.

## Etapas no servidor

1. Usuário confirma viabilidade do cadastro e entra na conta Oracle.
2. Conferir região, disponibilidade e recursos identificados como gratuitos antes de provisionar uma máquina virtual. Registrar arquitetura, sistema, IP e limites, sem guardar chaves privadas no Git.
3. Preparar acesso SSH pelo CMD do Windows. Os comandos executados dentro da sessão remota serão Linux, pois pertencem ao servidor, não ao CMD local.
4. Criar configuração Spring específica de hospedagem: banco e credenciais externos ao código, `ddl-auto=validate`, cookies Secure e Java restrito à interface local atrás do proxy. Não reutilizar as credenciais root sem senha do desenvolvimento.
5. Instalar Java 25 e MariaDB no servidor. Conferir versão e compatibilidade com o esquema antes de importar dados. O banco não ficará exposto à internet.
6. Criar banco remoto e usuário da aplicação com permissões limitadas. Definir com o usuário se a publicação usará uma cópia dos registros atuais ou um conjunto próprio para demonstração. Não transferir backup com dados pessoais implicitamente.
7. Aproveitar o empacotamento integrado existente, transferir o JAR e configurar a aplicação para iniciar e reiniciar como serviço. A instalação do banco e a aplicação continuarão simples, na mesma máquina.
8. Definir endereço público e HTTPS. Um domínio próprio não foi comprado nem é requisito autorizado; selecionar um endereço viável antes de configurar certificados. O IP público, sozinho, não conclui a configuração HTTPS.
9. Validar páginas, login/sessão, permissões e fluxos com registros identificados de demonstração. Confirmar acesso em outra máquina e funcionamento com o backend local desligado.
10. Configurar backup remoto e documentar atualização, reinício, restauração e limites da hospedagem. Commit e push exigem autorização específica conforme AGENTS.md.

## O que permanece preservado

Os bancos locais `tech_help` e `tech_help_teste`, as contas existentes e a configuração de desenvolvimento permanecem preservados. A publicação ainda não foi realizada. Não há conta criada pelo agente, recursos contratados, importação remota, mudança de esquema ou nova execução de testes nesta etapa de planejamento.

Atualização em 12/10/2026: cadastro Oracle ainda não confirmado; chamado de criação de conta aberto, e-mail recebido apenas confirma a fila. Aguarda orientação do atendimento. Detalhes pessoais do suporte não devem ir ao Git.

Preparação local implementada: perfil hospedagem, documentação de variáveis externas, modelo de serviço Linux e procedimento de atualização. Executados cinco testes HospedagemConfiguracaoTests, todos aprovados, sem conexão com banco. O script preparar-hospedagem.cmd também passou e gerou o JAR integrado; não houve nova suíte completa nem teste no servidor. Este conjunto compõe o checkpoint autorizado pelo usuário; confirmar commit e envio pelo Git.

Próxima dependência externa: cadastro/acesso ao servidor. Com acesso disponível, validar os modelos preparados no Linux real e configurar o endereço HTTPS. Modelo recomendado: Sol — raciocínio médio; revisão das permissões e da exposição do servidor pode exigir Astra — raciocínio médio.
