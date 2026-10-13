# Demonstração acadêmica sem hospedagem paga

## Opção recomendada

O usuário prefere custo zero e finalidade acadêmica. Para esse uso, um Cloudflare Quick Tunnel pode fornecer um endereço HTTPS temporário para a aplicação rodando no computador. Não exige domínio ou conta; o link muda a cada execução e depende do computador, MariaDB e aplicação ligados. Não oferece garantia de disponibilidade e não representa hospedagem permanente.

Referência oficial consultada em 11/10/2026: https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/ . O serviço admite até 200 requisições simultâneas e não suporta SSE; o MVP atual não usa SSE. Não substituir MariaDB por outro banco apenas para aproveitar plano gratuito de hospedagem.

## Pacote integrado

`preparar-demonstracao.cmd` compila React com VITE_API_URL=/ e usa o perfil Maven demonstracao para copiar frontend/dist para static dentro do JAR. Não modifica a configuração habitual do Vite em localhost:5173. O pacote integrado serve a interface e as APIs no mesmo endereço, preservando sessão e CSRF. A Home não usa rotas de URL adicionais.

O perfil Spring demonstracao inicia em 127.0.0.1:8082 e mantém ddl-auto=validate e o banco local configurado. SQL no console desativado nesse perfil. Arquivos estáticos públicos são liberados somente para GET; APIs privadas continuam autenticadas. Cloudflare encaminha HTTP local, enquanto visitantes acessam HTTPS. Cookies Secure ficam ativos no modo https e desativados apenas no modo local para conferir pelo HTTP.

## Preparar e conferir pelo CMD

Com Node, Java 25 e MariaDB disponíveis:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
preparar-demonstracao.cmd
iniciar-demonstracao.cmd local
```

Abrir http://127.0.0.1:8082 . O script usa JAVA_HOME já configurado ou a instalação conhecida jdk-25.0.4. O empacotamento não executa testes; eles são verificados separadamente. Se um clean posterior remover o pacote ou seus recursos, repetir a preparação.

Para encerrar, Ctrl+C no CMD e confirmar encerramento do arquivo em lotes quando solicitado. O backend habitual no NetBeans/8080 não precisa ser encerrado para conferir 8082.

## Abertura do link temporário

Em 11/10/2026, após a preparação, o usuário autorizou explicitamente abrir o link temporário. Quem tiver o link pode abrir os recursos públicos e cadastrar contas; ações privadas continuam exigindo login. Usa os dados do banco local configurado.

cloudflared 2026.10.0 obtido pelo download oficial da Cloudflare para Windows 64 bits, armazenado em .local-tools/cloudflared.exe (pasta excluída do Git), sem instalação como serviço. Para repetir a abertura, encerrar a conferência local e iniciar:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
iniciar-demonstracao.cmd https
```

Em outro CMD, com cloudflared disponível:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
.local-tools\cloudflared.exe tunnel --url http://127.0.0.1:8082
```

Usar somente o link HTTPS apresentado pelo programa para entrar na conta nesse modo. Cookies Secure não funcionam no endereço HTTP de conferência. Encerrar cloudflared ao terminar elimina o acesso pelo link. Não divulgar porta do MariaDB, não abrir portas no roteador e não distribuir senhas administrativas aos visitantes.

O perfil demonstracao reconhece os cabeçalhos de proxy (forward-headers-strategy=framework). Isso é necessário porque HTTPS termina na Cloudflare, e o HTTP local não pode ser confundido com uma origem externa no carregamento dos módulos JavaScript. Binding em 127.0.0.1 é mantido; não usar esse perfil em um servidor diretamente acessível sem avaliar quais proxies são confiáveis.

Para fechar os túneis que usam exclusivamente o executável desta pasta, pelo CMD:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help"
encerrar-link-temporario.cmd
```

Esse comando não encerra cloudflared de outra pasta e não encerra o backend habitual. Para processos iniciados manualmente, também pode usar Ctrl+C em seus CMDs. Não foi contratado serviço nem criada hospedagem permanente.
