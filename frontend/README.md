# Frontend TechHelp

Home em React + TypeScript com a direção visual aprovada: grafite, off-white e laranja. Usa as dependências que já existiam no projeto.

A referência atual é a [prévia integrada fornecida pelo usuário](../docs/referencias/TechHelp_Previa_Integrada.html). A Home incorpora notebook animado em canvas, explorador de serviços ilustrado, quatro passos acompanhando a rolagem e as seções de aluguel/profissionais dessa prévia. `src/features/home/` reúne apresentação, estilos e ilustrações; `Home.tsx` conserva os fluxos reais de sessão, publicação e propostas. O HTML de referência contém simulações históricas e não é usado para servir a aplicação.

## Executar

Use Node.js 22.18+ ou 24 e npm. Na pasta `frontend`:

```sh
npm ci
npm run dev
```

O backend deve estar em `http://localhost:8080`. Para outro endereço, configure `VITE_API_URL` em um arquivo `.env.local` (não coloque senhas nesse arquivo). Abra o endereço informado pelo Vite.

## O que esta entrega faz

- Consulta `GET /categorias` e exibe somente categorias ativas reais.
- Mostra carregamento, falha com nova tentativa ou catálogo vazio, sem substituir por dados fictícios.
- Guia pedidos de Hardware, Redes e Software em quatro etapas: problema, atendimento, detalhes e revisão.
- Adapta perguntas às respostas, restringe atendimento remoto e permite revisar e editar.
- Guarda o rascunho em `sessionStorage`: sobrevive ao recarregamento da mesma aba e é removido quando a sessão da aba termina. Se o navegador bloquear o armazenamento, a interface informa que as respostas só duram enquanto a página estiver aberta.
- Oferece menu mobile, controles de teclado, redução de movimento e pausa de animações.
- Cadastra clientes e técnicos e autentica por sessão, com cookie e proteção CSRF. O cadastro concluído encaminha ao login.
- Restaura a sessão ao abrir a página e permite sair. Senha e CPF não são salvos no rascunho nem em armazenamento local.
- Publica solicitações reais com o `idCliente` obtido da sessão. Respostas e região presencial entram na descrição; atendimento e urgência são convertidos para os valores da API.
- Mantém o rascunho durante cadastro/login e erros; limpa somente após confirmação do servidor. Exibe o número do pedido e a lista Meus pedidos.
- Bloqueia cliques repetidos durante envio. Em falha sem confirmação, pede conferir Meus pedidos antes de tentar novamente; não repete POST automaticamente.
- Oferece Área do técnico com oportunidades abertas ou em negociação, envio de valor/condições/prazo/disponibilidade e consulta das próprias propostas.
- Permite ao cliente abrir as propostas de seus pedidos, comparar condições e confirmar o aceite. O aceite cria um serviço e recusa as outras propostas enviadas; não realiza pagamento.

## Limites atuais

O backend deve incluir a autenticação descrita em [AUTENTICACAO.md](../docs/AUTENTICACAO.md). Use `localhost` tanto na interface quanto na URL da API para preservar o cookie SameSite. A origem de desenvolvimento autorizada pelo backend é `http://localhost:5173`.

A publicação exige uma conta com perfil CLIENTE. A área de oportunidades exige perfil TECNICO. A região é texto na descrição, sem criação de endereço completo ou geolocalização. Ainda não há recuperação de senha nem verificação de e-mail. Não há garantia de idempotência no servidor: a conferência após falha de rede reduz, mas não elimina, o risco de publicação duplicada. Escritas de propostas/aceite também exigem atualizar a consulta após falha sem confirmação.

Perfis completos, execução do serviço, avaliações e aluguel ainda não estão integrados à interface. Na comparação, os técnicos são identificados pelo número retornado pela API. Não há garantia contra aceites simultâneos em transações concorrentes. Sem pessoas, notas, preços ou estoque fictícios. Categorias além das três jornadas principais informam que suas perguntas ainda estão em preparação.

## Organização

- `src/pages/Home.tsx`: Home, consulta de categorias, navegação e armazenamento do rascunho.
- `src/features/solicitacao/RequestAssistant.tsx`: interface e revisão das quatro etapas.
- `src/features/solicitacao/flow.ts`: perguntas condicionais, validações, dependências e leitura segura do rascunho.
- `src/App.css`: identidade visual, layout responsivo e animações com CSS.
- `src/services/api.ts`: cliente HTTP com sessão, CSRF, login e tratamento de erros.
- `src/features/auth/AuthDialog.tsx`: cadastro de cliente/técnico e login.
- `src/features/solicitacao/publication.ts`: conversão validada do rascunho para a API.
- `src/features/solicitacao/MyRequests.tsx`: consulta dos pedidos do cliente conectado.
- `src/features/propostas/TechnicianBoard.tsx`: oportunidades e propostas do técnico.
- `src/features/propostas/ClientProposals.tsx`: comparação e confirmação de aceite pelo cliente.
- `src/features/propostas/proposals.ts`: conversão de valores, validação e apresentação de propostas.

Ao mudar uma resposta que altera as perguntas seguintes, apenas as respostas dependentes e o tipo de atendimento são invalidados. Região e detalhes continuam guardados. As respostas das outras categorias também são mantidas durante a troca.

## Verificação

```sh
npm run build
npm run lint
npm test
```

Os 14 testes usam o executor nativo do Node, sem biblioteca adicional. Cobrem as regras condicionais, bloqueio de remoto, edição e restauração do rascunho, conversão para publicação, identidade do cliente, envio de cookies/CSRF, formato do login, sessão ausente e ausência de repetição automática de escritas, além de conversão/validação de propostas e estados que permitem negociação. Os testes HTTP usam um adapter simulado e não comprovam conexão real.

A validação manual em navegador com `tech_help_teste` cobriu cadastro de cliente/técnico, senha incorreta, login, preservação do rascunho, publicação real, consulta do pedido, restauração da sessão, limpeza do rascunho e logout. A conta de técnico foi impedida de publicar como cliente. Usar dados temporários identificados e preservar os dados preexistentes ao repetir essa verificação.

Na etapa de propostas (26/09/2026), foram verificados envio pelo técnico, comparação de duas propostas, cancelamento da confirmação, aceite pelo cliente, estados persistidos após atualizar e visualização da proposta aceita pelo técnico. SQL confirmou a criação de um único serviço e a recusa da alternativa. Os registros temporários foram removidos; dados anteriores preservados. Detalhes e limites estão em [CONTEXTO_DO_PROJETO.md](../docs/CONTEXTO_DO_PROJETO.md).

Para verificar a integração no seu computador: ligue o MariaDB e o backend, abra a Home e confira as categorias. Depois pare o backend e recarregue: a tela deve exibir falha com nova tentativa, sem dados inventados. Teste também uma resposta vazia da API, revisão, troca de categoria, recarregamento da aba e navegação no celular.
