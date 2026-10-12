# Frontend TechHelp

## Manutenção do catálogo

Em **Administrar catálogo**, use **Editar ferramenta**, ou selecione um kit para editar seus dados. Revise e confirme nome, descrição, diária, situação e, para ferramentas, quantidade total. Ferramentas podem ficar disponíveis, em manutenção ou inativas; kits disponíveis ou inativos. Inativação preserva registros e aluguéis anteriores. Todo ajuste fica bloqueado enquanto o item participa de aluguel ativo.

O ajuste de quantidade preserva unidades anteriormente indisponíveis. Manutenção/inativação de ferramenta impede novas reservas dos kits que a usam; não representa manutenção parcial de unidades. Alterar diária não recalcula aluguéis já registrados. Escritas exigem ADMIN e CSRF, com confirmação e consulta obrigatória após falha, sem repetição automática. Reinicie o backend atualizado para carregar os endpoints PUT.

Conferência manual: anotar os dados originais de um item TESTE livre, editar diária/descrição, confirmar persistência, colocar ferramenta em manutenção e conferir a retirada do kit do catálogo disponível; restaurar os dados/situação originais. Repetir inativação/reativação do kit. Preservar histórico e não apagar cadastros. Validar também a recusa de edição durante uma reserva ativa.

## Administração de retirada e devolução

Contas ADMIN acessam **Administrar aluguéis**. A tela consulta GET /alugueis/administracao?pagina=0 (20 registros por página, mais recentes primeiro), apresenta o nome do usuário, kits, valores, datas previstas e efetivas. Registre retirada somente após entregar os equipamentos e devolução após recebê-los e conferi-los; cada ação exige confirmação. Reaproveita os endpoints administrativos existentes e reconfirma sessão/perfil antes de operar, usando CSRF, sem repetição automática após falha. Após erro, atualizar a lista é obrigatório.

A indicação de devolução vencida é calculada no relógio de Brasília ao renderizar a tela; não muda o status persistido, não aplica multa e deve ser atualizada pelo botão de consulta. Histórico cancelado/devolvido permanece consultável. Cartões e controles se adaptam a telas pequenas.

Roteiro: reiniciar backend atualizado, entrar como ADMIN, consultar páginas e histórico; usar uma nova reserva identificada de teste para confirmar retirada, verificar a data/status, confirmar devolução e consultar disponibilidade. Preservar registros anteriores. Conferir também que CLIENTE/TECNICO não acessam a consulta geral nem registram retirada/devolução. A lista não inclui senha, documento ou contato do usuário.

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

Perfis completos e aluguel ainda não estão integrados à interface. Meus serviços permite acompanhamento pelo cliente e início/conclusão pelo técnico responsável; o técnico pode registrar a data combinada e ambos os participantes podem cancelar antes do início. Na comparação e nos serviços do cliente, técnicos são identificados pelo nome cadastrado quando disponível; o número permanece como referência alternativa. Não há garantia contra aceites simultâneos em transações concorrentes. Sem pessoas, notas, preços ou estoque fictícios. Categorias além das três jornadas principais informam que suas perguntas ainda estão em preparação.

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
- `src/features/servicos/MyServices.tsx`: acompanhamento por cliente/técnico, confirmação de início e conclusão e atualização após falhas.
- `src/features/servicos/services.ts`: estados, apresentação de datas e execução com reconferência da sessão.

Ao mudar uma resposta que altera as perguntas seguintes, apenas as respostas dependentes e o tipo de atendimento são invalidados. Região e detalhes continuam guardados. As respostas das outras categorias também são mantidas durante a troca.

## Verificação

```sh
npm run build
npm run lint
npm test
```

Os 24 testes usam o executor nativo do Node, sem biblioteca adicional. Cobrem as regras condicionais, bloqueio de remoto, edição e restauração do rascunho, conversão para publicação, identidade do cliente, envio de cookies/CSRF, formato do login, sessão ausente e ausência de repetição automática de escritas, além de conversão/validação de propostas e estados que permitem negociação. Também cobrem ações por responsável/estado, troca de sessão e ausência de repetição automática após timeout ao executar serviço. Os testes HTTP usam um adapter simulado e não comprovam conexão real.

A validação manual em navegador com `tech_help_teste` cobriu cadastro de cliente/técnico, senha incorreta, login, preservação do rascunho, publicação real, consulta do pedido, restauração da sessão, limpeza do rascunho e logout. A conta de técnico foi impedida de publicar como cliente. Usar dados temporários identificados e preservar os dados preexistentes ao repetir essa verificação.

Na etapa de propostas (26/09/2026), foram verificados envio pelo técnico, comparação de duas propostas, cancelamento da confirmação, aceite pelo cliente, estados persistidos após atualizar e visualização da proposta aceita pelo técnico. SQL confirmou a criação de um único serviço e a recusa da alternativa. Os registros temporários foram removidos; dados anteriores preservados. Detalhes e limites estão em [CONTEXTO_DO_PROJETO.md](../docs/CONTEXTO_DO_PROJETO.md).

Para verificar a integração no seu computador: ligue o MariaDB e o backend, abra a Home e confira as categorias. Depois pare o backend e recarregue: a tela deve exibir falha com nova tentativa, sem dados inventados. Teste também uma resposta vazia da API, revisão, troca de categoria, recarregamento da aba e navegação no celular.

## Meus serviços

Depois de entrar, use **Meus serviços** no menu. O cliente consulta os atendimentos contratados; o técnico responsável confirma início e conclusão. Contas com ambos os perfis podem alternar entre as duas listas. Use **Atualizar serviços** para consultar mudanças feitas pela outra pessoa.

O status interno `AGENDADO` aparece como **Aguardando início**: o aceite da proposta não preenche automaticamente `dataAgendada`. A interface preserva o horário local recebido do backend. Não existe confirmação de pagamento nem agendamento automático nessa operação.

Para conferir o fluxo completo, aceite uma proposta como cliente, entre com o técnico responsável, inicie e conclua o atendimento. Retorne à conta do cliente e atualize Meus serviços e Meus pedidos: ambos devem mostrar a conclusão. Execute esse roteiro apenas em registros de teste identificados. Após a conclusão, o cliente pode abrir a avaliação no próprio cartão do serviço.

## Avaliação do atendimento

Em Meus serviços, um atendimento concluído oferece **Avaliar ou consultar avaliação** ao cliente. A consulta verifica se já existe avaliação antes de abrir o formulário. A nota é obrigatória (1 a 5); o comentário é opcional (até 3000 caracteres). Depois de enviar, a avaliação registrada substitui o formulário. Edição e exclusão não são oferecidas nesta tela.

O técnico pode usar **Consultar avaliação** no serviço concluído para ler a nota e o comentário recebidos. Após falha no envio, a interface mantém o texto e exige nova consulta antes de permitir outra tentativa. O servidor identifica cliente e técnico pelo serviço; a interface envia somente nota/comentário, com sessão e CSRF. Os testes de transporte são simulados, não equivalem a um novo teste no navegador com MariaDB.

## Identificação dos técnicos

As propostas, a confirmação de contratação e os serviços do cliente consultam `GET /tecnicos/resumos?ids=20,21`. A resposta pública contém somente `idTecnico` e `nome`, para contas ativas com nome preenchido. Identificadores repetidos são agrupados e consultas maiores são divididas em lotes de até 50. Nenhum e-mail, CPF, telefone ou hash de senha é necessário para essa apresentação.

Se a consulta falhar ou não retornar um nome, a interface mantém `Técnico #ID` e permite continuar o fluxo. Atualizar propostas/serviços também renova a consulta dos nomes. Isso não representa perfil profissional completo nem muda a identificação de clientes na área do técnico.

## Agendamento e cancelamento

Em Meus serviços, o técnico responsável usa **Agendar atendimento** ou **Alterar agendamento** enquanto o serviço ainda aguarda início. A data deve ser futura, no horário de Brasília. Trata-se do registro de um horário previamente combinado; não há confirmação da outra parte pela plataforma, calendário de disponibilidade nem bloqueio de horários sobrepostos. O início continua permitido sem agendamento registrado, inclusive para atendimentos antigos.

Cliente e técnico podem usar **Cancelar atendimento** antes do início. A confirmação cancela o serviço e o pedido na mesma transação. O pedido não volta à lista de oportunidades: para contratar novamente, o cliente cria outro. As propostas mantêm o histórico do aceite/recusa. Atendimentos iniciados, concluídos ou já cancelados rejeitam cancelamento e reagendamento. Serviços cancelados não permitem início, conclusão ou avaliação. A tela exige atualizar os dados após falhas, sem repetir automaticamente a escrita.

## Catálogo administrativo

Entre com uma conta que já tenha o perfil ADMIN e use **Administrar catálogo** no menu. O cadastro público não concede esse perfil.

1. Cadastre uma ferramenta com nome, descrição, quantidade total e diária.
2. Cadastre um kit com nome, descrição e diária própria.
3. Selecione o kit e adicione suas ferramentas, indicando unidades por kit.
4. Para remover um componente, use **Remover do kit** e confirme. Kits com aluguel ativo rejeitam alterações.
5. Após falhas, use **Atualizar catálogo** e confira os registros antes de repetir uma operação.

A tela distingue quantidade total cadastrada de saldo livre. Kits vazios não podem ser reservados. Não inclui edição dos dados cadastrais, exclusão de ferramentas/kits, reservas pelo cliente ou administração de contas. A sessão é reconfirmada antes das escritas, com token CSRF inclusive no DELETE e sem repetição automática após timeout.

A verificação manual deve usar registros de teste identificados e preservar dados anteriores. Conferir acesso com ADMIN e ausência do menu para CLIENTE/TECNICO, cadastro, seleção de kit, inclusão/remoção e falha de rede. Esta etapa não promoveu usuários a administrador nem populou o catálogo real.

## Reserva de kits pelo usuário

A Home (**Conhecer o aluguel**) abre o catálogo público. Usuários autenticados também acessam **Meus aluguéis** no menu. Escolha um kit, quantidade e datas futuras no horário de Brasília; revise e confirme. A tela informa uma estimativa; o backend confirma valor e estoque. A regra atual cobra a diferença entre as datas, com mínimo de uma diária. Não há pagamento online, entrega ou reserva por janelas de disponibilidade nesta etapa.

A nova operação POST /alugueis/reservar grava aluguel e item juntos. Reinicie o backend após atualizar o código para carregar esse endpoint. Reservas RESERVADO podem ser canceladas pelo próprio usuário; retirada/devolução continuam operações administrativas no backend.

Verificação manual: usar kit identificado como TESTE, conferir total, lista pessoal, cancelamento e retorno ao catálogo. Testar sessão encerrada e falta de estoque. Nunca reenviar automaticamente após timeout; atualizar a lista antes. O teste automatizado de concorrência utiliza fixtures próprias e não deve apagar registros do usuário.
