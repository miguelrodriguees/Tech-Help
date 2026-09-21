# Revisão de solicitações, propostas e atendimento

Base revisada: branch feat/fluxo-principal-validacao, commit 6735a91. Revisão estática; não homologação de produção.

## Permissões encontradas no código

- ClienteAutenticado e TecnicoAutenticado derivam IDs da sessão e exigem usuário ATIVO.
- Publicação usa cliente da sessão, categoria ativa, endereço pertencente ao cliente e orçamento coerente.
- Listagem de pedidos/propostas/serviços próprios usa identidade da sessão; consulta de propostas exige dono do pedido.
- Solicitações ABERTA e EM_NEGOCIACAO são visíveis a técnicos para receber propostas, exceto pedidos próprios. Isso é a regra de descoberta do marketplace, não uma lista restrita ao dono.
- Envio de proposta exige técnico da sessão e pedido aberto/negociando; rejeita autoatendimento e duplicada.
- Contratação exige dono do pedido, transação e trava na solicitação compartilhada com envio. Aceita uma e recusa outras enviadas. Repetição da mesma contratação retorna atendimento existente.
- Só técnico contratado inicia, só cliente do serviço conclui/avalia. Consulta da avaliação exige participante.
- Endpoints legados que aceitam IDs arbitrários continuam bloqueados por denyAll. Não liberar globalmente essas rotas ao depurar.

## Ordem das operações

AGENDADO → EM_ANDAMENTO → CONCLUIDO. Iniciar/concluir repetidos no mesmo estado não regravam datas. Conclusão atualiza pedido para CONCLUIDA dentro da transação externa. Avaliação exige serviço concluído, participantes distintos e ausência de avaliação anterior. Lock do serviço serializa avaliações recebidas pelo novo endpoint. Concorrência e rollback precisam de testes reais no MariaDB; testes com mocks não os comprovam.

## Correções desta rodada

- Renovar login do mesmo cliente mantém a tela do pedido aberta, em vez de voltar à Home. Troca de usuário continua remontando a área pela chave idUsuario, sem reaproveitar dados da conta anterior.
- Atualizar listas de propostas/atendimentos mantém componentes de formulário montados. Campos em preenchimento sobrevivem à atualização, enquanto comandos ficam desabilitados durante carregamento. Falhas diretas de envio mantêm campos. Não há persistência desses formulários em recarregamento completo/fechamento da página; apenas solicitação possui rascunho em sessionStorage.
- Lista de pedidos permite tentar novamente após sessão expirada.
- Contratação e conclusão atualizam também o status exibido no pedido, sem esperar reabrir a página.

## Verificação e limites

Build, lint e nove testes frontend existentes executados. Esses testes cobrem assistente/payload, não interação de recuperação da sessão. Seis testes Java novos cobrem leitura de propostas por dono/terceiro e transições reais de ServicoService; escritos, NÃO EXECUTADOS aqui. Backend, filtros HTTP/CSRF/CORS, navegador, concorrência e banco ainda precisam rodar no computador com Java 25/MariaDB.

Roteiro adicional de navegador: preencher proposta, atualizar lista e conferir campos; expirar sessão, entrar na mesma conta e reenviar; repetir com comentário de avaliação. Trocar para outro usuário e confirmar que a área não exibe dados da conta anterior. Contratar e concluir e conferir status no topo do pedido. Recarregar após resposta perdida e consultar dados antes de repetir comandos.

Limites ainda existentes: publicação só deduplica na sessão (uma sessão inteiramente nova pode permitir repetição após resposta perdida); sessão compartilhada entre abas exige cuidado ao trocar contas; agendamento, cancelamento e recuperação de senha ainda não conectados. Técnicos PENDENTE podem propor pela regra atual, sem alegação de credenciamento. Formulários não garantem preservação após navegar para outra página. Não houve alteração de banco nem do desenho da Home.
