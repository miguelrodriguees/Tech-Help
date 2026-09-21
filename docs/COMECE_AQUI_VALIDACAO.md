# Instalar a versão de validação no Windows

Branch: `feat/fluxo-principal-validacao`. Base remota: `feat/home-grafite-assistente`, commit `a814eb8`. Não é uma versão já validada de ponta a ponta. Nenhuma alteração foi aplicada ao banco existente.

## 1. Baixar em outra pasta

No CMD, execute (não dentro do terminal ocupado pelo Vite):

```cmd
cd /d "C:\Users\Miguel Rodrigues\Desktop"
git clone --branch feat/fluxo-principal-validacao --single-branch https://github.com/miguelrodriguees/Tech-Help.git Tech-Help-validacao
cd Tech-Help-validacao
git branch --show-current
java -version
node -v
```

A pasta antiga Tech-Help fica intacta. Se Tech-Help-validacao já existir, não apague nem sobrescreva: confira antes o conteúdo. Java 25.0.4 já foi confirmado neste computador. O frontend exige versão de Node aceita pelo Vite do package-lock; em caso de aviso de engine, guarde a mensagem antes de instalar qualquer versão.

## 2. Preparar banco separado

Com o MariaDB ligado:

```cmd
node scripts/preparar-banco-teste.mjs
```

Abra `backend/target/schema-test.sql` no DBeaver, na conexão MariaDB local. Configure a execução para parar no primeiro erro. Execute o script: ele cria **tech_help_teste**, tabelas e dados básicos de categorias/perfis, sem alterar tech_help. Se o banco já existir, pare e confira sua origem; não remova bancos para repetir o teste. O gerador não executa SQL. Antes de executar, confira que CREATE DATABASE e USE referem-se a tech_help_teste.

## 3. Testar e iniciar backend

Pare o backend antigo no NetBeans para liberar 8080. No CMD:

```cmd
cd backend
set SPRING_DATASOURCE_URL=jdbc:mariadb://127.0.0.1:3306/tech_help_teste
mvnw.cmd test
```

Só continue se terminar com BUILD SUCCESS. Se mvnw reclamar de JAVA_HOME, confira `echo %JAVA_HOME%` e `where java`; não reinstale automaticamente. Configuração local padrão usa root sem senha, conforme projeto existente; se sua conexão tem credenciais diferentes, defina SPRING_DATASOURCE_USERNAME e SPRING_DATASOURCE_PASSWORD nesta janela e não compartilhe a senha.

```cmd
mvnw.cmd spring-boot:run
```

Mantenha a janela aberta. O backend deve iniciar em 8080 com banco tech_help_teste. A variável definida vale nesta janela; iniciar via NetBeans sem essa configuração pode apontar ao banco antigo.

## 4. Teste automático da API

Abra outro CMD na raiz de Tech-Help-validacao:

```cmd
node scripts/validar-fluxo.mjs --banco-de-testes
```

Ele cria quatro contas sintéticas e registros no banco de testes. A flag confirma sua escolha, mas não detecta o nome do banco do servidor: confira a configuração do backend antes. Não use em produção. Guarde a saída completa; o script para no primeiro resultado inesperado. Esse teste ainda não foi executado pelo assistente.

## 5. Teste pelo navegador

Pare o Vite antigo (Ctrl+C na janela dele) para liberar 5173. Em outro CMD:

```cmd
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help-validacao\frontend"
npm ci
npm run build
npm run lint
npm test
npm run dev
```

Abra http://localhost:5173, usando localhost também no frontend (não IP 127.0.0.1). O backend configurado na API é localhost:8080. Teste com novas contas de teste: cliente publica; técnico propõe; cliente aceita; técnico inicia; cliente conclui e avalia. Como login é compartilhado entre abas do mesmo navegador, saia antes de trocar de conta, ou use janela anônima para o outro participante.

## Limites conhecidos

Build/lint e nove testes frontend passaram no ambiente do assistente. Java/backend/MariaDB e navegador não foram executados aqui por falta de runtime/dependências; os testes Java escritos precisam rodar no passo 3. Agendamento de horário, cancelamento, conversão cliente/técnico e aluguel ainda não estão conectados. As rotas legadas sem regras de autorização são bloqueadas intencionalmente. Para voltar ao projeto antigo, pare os servidores de teste e abra os servidores pela pasta original.
