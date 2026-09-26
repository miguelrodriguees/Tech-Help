# Frontend TechHelp

Home em React + TypeScript com a direção visual aprovada: grafite, off-white e laranja. Usa as dependências que já existiam no projeto.

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

## Limites atuais

**Não existe autenticação nem publicação de solicitações nesta entrega.** Entrar/Publicar mostram uma explicação verdadeira, sem simular login, enviar dados ou mostrar confirmação de sucesso. O backend de solicitações exige `idCliente`; não inventamos esse identificador nem usamos um cliente fixo. A futura publicação deve usar a identidade autenticada.

Perfis, propostas, avaliações e aluguel aparecem apenas como explicações. Sem pessoas, notas, preços ou estoque fictícios. Categorias além das três jornadas principais informam que suas perguntas ainda estão em preparação.

## Organização

- `src/pages/Home.tsx`: Home, consulta de categorias, navegação e armazenamento do rascunho.
- `src/features/solicitacao/RequestAssistant.tsx`: interface e revisão das quatro etapas.
- `src/features/solicitacao/flow.ts`: perguntas condicionais, validações, dependências e leitura segura do rascunho.
- `src/App.css`: identidade visual, layout responsivo e animações com CSS.
- `src/services/api.ts`: cliente HTTP existente, preservado.

Ao mudar uma resposta que altera as perguntas seguintes, apenas as respostas dependentes e o tipo de atendimento são invalidados. Região e detalhes continuam guardados. As respostas das outras categorias também são mantidas durante a troca.

## Verificação

```sh
npm run build
npm run lint
npm test
```

Os testes usam o executor nativo do Node, sem biblioteca adicional. Cobrem as regras condicionais, bloqueio de remoto para falhas físicas, edição e restauração do rascunho. Eles não comprovam conexão com um banco ou backend em execução.

Para verificar a integração no seu computador: ligue o MariaDB e o backend, abra a Home e confira as categorias. Depois pare o backend e recarregue: a tela deve exibir falha com nova tentativa, sem dados inventados. Teste também uma resposta vazia da API, revisão, troca de categoria, recarregamento da aba e navegação no celular.
