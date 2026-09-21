# Validação do fluxo principal

## Resultado desta rodada

- Build frontend: PASSOU.
- ESLint: PASSOU.
- Nove testes de assistente/payload: PASSARAM.
- Teste de navegador: NÃO EXECUTADO, Playwright disponível mas sem Chromium instalado.
- Backend/testes Java: NÃO EXECUTADOS, ambiente possui apenas Java 17 e acesso Maven indisponível.
- Banco e fluxo integrado: NÃO EXECUTADOS, sem servidor MariaDB neste ambiente.
- Script `scripts/validar-fluxo.mjs`: sintaxe verificada; execução integrada pendente.

A compilação frontend não comprova autorização, persistência nem transações. O Java 25.0.4 do computador do desenvolvedor foi confirmado no CMD. As modificações desta cópia de trabalho ainda não estão no GitHub nem automaticamente no computador dele.

## Ajustes feitos

Vite exige porta 5173, evitando mudança silenciosa para 5174 incompatível com o CORS configurado. ApiExceptionHandler devolve explicitamente status de autorização/propriedade e validação em JSON, sem depender do despacho de erro padrão para uma rota bloqueada pelo SecurityConfig.

## Teste real preparado

Requer código atualizado desta cópia, Node compatível com o frontend (com fetch e getSetCookie), Java 25, Maven Wrapper e banco MariaDB de testes com schema, perfis CLIENTE/TECNICO e categorias. O script cria quatro contas sintéticas e registros persistentes. Não usa nem apaga contas existentes. Não executar contra banco de produção.

1. Criar banco de testes separado usando o schema do projeto; revisar o SQL antes, pois ele contém seleção de banco e comandos de recriação. Não executar o script SQL original cegamente contra banco existente.
2. Em um CMD na pasta backend, configurar `set SPRING_DATASOURCE_URL=jdbc:mariadb://127.0.0.1:3306/tech_help_teste` (nome do banco realmente preparado), usuário/senha se necessário.
3. Executar `mvnw.cmd test` e depois `mvnw.cmd spring-boot:run` nessa mesma janela.
4. Em outro CMD na raiz Tech-Help: `node scripts/validar-fluxo.mjs --banco-de-testes`.

O teste verifica cadastro/login/CSRF, publicação e repetição, duas propostas, bloqueio de duplicadas e de terceiros, contratação/repetição, rejeição de segunda contratação, início, conclusão, avaliação e nota inválida. Se falhar, imprime a rota e o status; não imprime senha. Registros parciais permanecem no banco de testes para investigação. Não prova concorrência de requisições nem layout, acessibilidade ou comportamento CORS de navegador.

Após API passar, iniciar frontend com `npm run dev` em localhost:5173 e repetir as ações pelo navegador com contas de teste. Confirmar cookie/CORS, preservação de formulário em erro, expiração de sessão e tela mobile. Concorrência real ainda precisa de um teste próprio antes de publicação.
