# Contexto e continuidade do Tech Help

## Conferência por perfil e checkpoint autorizado — 11/10/2026

Criado FluxoMvpIntegracaoTests opt-in, com MockMvc, sessão/login reais e MariaDB tech_help. Teste novo executado separadamente: passou cadastro, login, publicação, proposta, aceite, início, conclusão, avaliação única e logout, com bloqueios por anonimato, CSRF e perfil incorreto. Datas verificadas nas respostas. Fixtures UUID próprias removidas; banco voltou a 3 usuários, 1 solicitação, 1 proposta, 1 serviço, 0 avaliações e zero usuários/categorias TESTE-FLUXO. Não houve nova execução da suíte completa; os 72 testes anteriores continuam evidência da etapa anterior.

Usuário entrou sucessivamente como cliente, técnico e ADMIN. Conferência em leitura das telas existentes: cliente viu pedido 1 contratado, proposta aceita/R$ 200, serviço 1 aguardando início e aluguéis 4/5 cancelados; técnico viu proposta aceita, nenhuma oportunidade aberta e ações de agendar/iniciar/cancelar; ADMIN viu ferramenta 5/5/R$ 5, kit/composição e histórico 4/5 cancelados e 6 devolvido. Nenhum atendimento anterior iniciado/concluído/cancelado nesta conferência. Capturas em docs/evidencias/cliente-*, tecnico-* e admin-*-conferencia-final.jpg. Isto não equivale a executar todos os formulários pelo navegador nem teste em celular físico.

Checkpoint preparado em CHECKPOINT_MVP.md; usuário autorizou explicitamente criar o commit local `feat: conclui fluxos locais de serviços e aluguel do MVP`, incluindo fontes, testes, documentação e evidências, sem push. Registro do hash e estado final devem ser confirmados no Git após a operação. Último commit anterior era e45b296, branch feat/home-grafite-assistente. Escopo local do MVP permanece retirada/devolução e pagamento fora do site. Preparação para hospedagem pública e melhorias adiadas continuam no roteiro; não declarar publicação realizada.

## Continuidade autônoma e fechamento de integridade — 11/10/2026

Usuário autorizou avançar pelas pendências sem perguntar a cada etapa; preferência também registrada em AGENTS.md. Aprovou explicitamente retirada local e pagamento fora do site no MVP; entrega com taxa e pagamento online adiados. Escopo e limites consolidados em FECHAMENTO_MVP.md.

AvaliacaoService agora tem transações READ_COMMITTED nas duas entradas (inclusive avaliarAtendimento, que chama criar internamente) e bloqueia/relê o serviço antes de verificar duplicidade. Novo AvaliacaoConcorrenciaTests disputa a entrada nova e a antiga: apenas uma avaliação é gravada e a outra recebe erro de regra. DTOs de cadastro limitam nome 120, e-mail 254 e telefone 20; valores de solicitação/proposta/criação de ferramenta e kit limitados a 10 dígitos inteiros e 2 decimais. Comentário da avaliação antiga limitado aos mesmos 3000 caracteres da entrada nova.

Datas de criação de solicitação, proposta, serviço e avaliação agora retornam após flush/refresh dentro da transação. A tentativa com @Generated foi descartada: gerou INSERT RETURNING incompatível com MariaDB 10.4. Não restaram essas anotações, mudanças de Dialect ou de esquema. PropostaConcorrenciaTests e AvaliacaoConcorrenciaTests conferem datas nas respostas reais.

ReservaIntegracaoTests ampliado com reserva versus manutenção da ferramenta e reserva versus inativação do kit. Testa também recusa de manutenção quando há reserva ativa. Fixtures isoladas com UUID, conta temporária sem senha utilizável, limpeza restrita aos IDs próprios após encerrar threads. Nenhuma conta preexistente promovida.

Verificação final: compilação e suíte completa de 72 testes do backend passaram (48 segurança, 15 estoque, 3 propostas, 3 reserva, 1 avaliação, 2 contexto), integrações opt-in habilitadas no tech_help. Frontend: 36 testes, build e lint passaram após liberar subprocessos bloqueados por EPERM. Depois, pequena alteração de texto em RentalPage informa pagamento fora do site e ausência de cobrança online; conferência de interface em andamento. Consulta SQL final: 3 usuários, 1 solicitação, 1 proposta, 1 serviço, 0 avaliações e nenhum usuário TESTE-% restante. Kit 1 continua disponível/R$ 5,00; aluguéis 4/5 cancelados e 6 devolvido, valores preservados.

Usuário confirmou reinício do backend no NetBeans. Sessão antiga estava expirada; página recarregada como visitante. Catálogo público carregou kit TESTE disponível a R$ 5,00 e texto de pagamento fora do site. Conferência em viewport 390x844: conteúdo legível, sem transbordamento horizontal observado; tamanho restaurado. Evidências aluguel-pagamento-fora-site.jpg e aluguel-mvp-mobile.jpg. Não houve novo percurso autenticado no navegador nem teste em celular físico. Alterações locais sem commit/push; GitHub não consultado. Próximo bloco: conferência final dos percursos autenticados e checkpoint revisável, Sol — raciocínio médio; não solicitar novamente autorização para prosseguir com o desenvolvimento.

## Aceitação concorrente protegida e testada — 11/10/2026

ServicoService.aceitarProposta agora bloqueia e relê a solicitação e depois a proposta antes das validações; PropostaService.criar usa o mesmo bloqueio da solicitação. Ambas as operações usam transação READ_COMMITTED. Preservados os métodos de autorização, endpoints e regras de status. Nenhuma alteração de esquema. A brecha anterior foi identificada por leitura, sem reprodução em versão desprotegida.

Compilação e 49 testes passaram nesta etapa: 46 de segurança e 3 novos PropostaConcorrenciaTests opt-in no MariaDB tech_help. Cenários: duas propostas diferentes, mesma proposta duas vezes e envio durante aceite, com entidades carregadas antes da barreira entre threads. Resultado: único serviço/aceite, pedido contratado, nenhuma proposta pendente e técnico/valor consistentes. Fixtures UUID próprias removidas; contagens anteriores preservadas (3 usuários, 1 solicitação, 1 proposta, 1 serviço), zero usuários/categorias temporários. Detalhes e comando CMD em CONCORRENCIA_PROPOSTAS.md.

Não houve teste de carga, navegação ou execução da suíte completa. Backend do NetBeans precisa ser reiniciado para carregar a mudança; os testes usaram outro contexto Spring. Aviso já existente de suporte ao MariaDB 10.4.32 permanece. Alterações salvas localmente, sem commit/push; GitHub não consultado. Próxima etapa recomendada: verificar e proteger avaliações duplicadas sob requisições simultâneas. Modelo recomendado: Astra — raciocínio médio.

## Edição e manutenção validadas no site e MariaDB — 11/10/2026

Após reiniciar o backend, a tentativa inicial foi bloqueada pela reconfirmação de sessão expirada, sem alterar o banco. O usuário entrou novamente como ADMIN e o agente concluiu o teste reversível pela interface, usando exclusivamente ferramenta 1 e kit 1 marcados TESTE. Banco efetivo confirmado como tech_help antes das escritas; não havia aluguel ativo.

Ferramenta: descrição temporária identificada, diária R$ 6,50, total/disponível 6/6 e MANUTENCAO persistidos pelo PUT; o kit dependente deixou de aparecer em Kits disponíveis. Restaurados descrição TESTE, diária R$ 5,00, total/disponível 5/5 e DISPONIVEL. Kit: diária temporária R$ 7,00 e INATIVO persistidos; novamente ausente do catálogo de reservas. Restaurados diária R$ 5,00 e DISPONIVEL; reapareceu na interface. Nomes e composição de uma unidade da ferramenta preservados.

SQL final confirmou os valores originais e os aluguéis 4 CANCELADO/R$ 20,00, 5 CANCELADO/R$ 10,00 e 6 DEVOLVIDO/R$ 10,00, todos os itens mantendo diária R$ 5,00. Nenhum registro excluído, reserva nova criada ou esquema alterado. Evidências: docs/evidencias/catalogo-manutencao-bloqueio.jpg, catalogo-kit-inativo.jpg e catalogo-restaurado.jpg.

Esta retomada comprovou os PUTs reais e o percurso básico de manutenção/inativação/restauração; não testou disputa simultânea de edição e reserva nem bloqueio com aluguel ativo no MariaDB (este último possui cobertura com mocks da etapa anterior). Não houve mudança de código nem nova execução da suíte automatizada. Documentação e evidências locais, sem commit/push; situação atual no GitHub não consultada. Próxima etapa recomendada: revisar e testar a aceitação concorrente de propostas de serviço, preservando a estrutura existente. Modelo recomendado: Astra com raciocínio médio.

## Manutenção do catálogo implementada — 11/10/2026

Adicionados PUT /ferramentas/{id} e PUT /kits/{id}, DTOs AtualizarFerramentaRequest/AtualizarKitRequest e formulários de edição em AdminCatalog. Apenas ADMIN pode editar; sessão/perfil reconfirmados na interface, CSRF no PUT, revisão/confirmação e bloqueio de repetição durante envio. Após falha exige atualizar o catálogo. Ferramentas: nome, descrição, diária, total e situações DISPONIVEL/MANUTENCAO/INATIVA. Kits: nome, descrição, diária e DISPONIVEL/INATIVO; INDISPONIVEL continua controlado pelo aluguel. Não há exclusão de cadastros.

Serviços de edição usam READ_COMMITTED e bloqueio pessimista do registro. Ferramenta com quantidade comprometida e kit com aluguel ativo recusam toda edição; nomes duplicados e valores fora de precisão são rejeitados. Ajuste do total preserva a diferença entre total e disponível anterior; redução abaixo das unidades indisponíveis é recusada. Valores de Aluguel/AluguelItem anteriores não são atualizados. Kits que usam ferramenta em manutenção/inativa deixam de ser reserváveis pelo cálculo existente; não há manutenção parcial por unidade nesta versão. Nenhuma alteração de esquema/ddl-auto.

Verificações: 61 testes selecionados de backend (46 segurança, 15 estoque, mocks) e 36 testes Node passaram; build de produção e ESLint passaram. Corrigida distinção dos tipos editTool/editKit detectada pelo TypeScript antes do build final. Navegador: formulário de ferramenta abre preenchido com os dados reais; conferência a 390px sem transbordamento horizontal observado, evidência docs/evidencias/catalogo-edicao-mobile.jpg, viewport restaurado. Não houve gravação real pelo novo PUT, teste de concorrência da edição no MariaDB nem nova suíte de contexto. Usuário solicitado a reiniciar o backend; aguardar antes de escrita real. Próxima etapa: validar edição, manutenção/inativação e restauração nos itens TESTE preservando os valores e histórico originais. Modelo recomendado: Sol médio; revisão de disputas concorrentes, Astra médio. Alterações locais, sem commit/push.

## Retirada e devolução conferidas no site e MariaDB — 11/10/2026

Usuário criou pela interface a reserva de teste 6 na conta ADMIN (usuário 3), kit 1 TESTE, quantidade 1, período 12/10/2026 10:00 a 14/10/2026 10:00, total R$ 10,00. Banco efetivo conferido como tech_help; registros 4 e 5 cancelados usados como referência de preservação. O agente abriu Administrar aluguéis e simulou as operações físicas exclusivamente no registro 6, confirmando cada operação na interface.

Retirada: status RETIRADO e data_retirada 11/10/2026 20:33:04.514251 persistidos; kit continuou INDISPONIVEL. Devolução: status DEVOLVIDO e data_devolucao 11/10/2026 20:33:28.288474 persistidos; kit voltou a DISPONIVEL e reapareceu no catálogo. Contadores da ferramenta mantidos em 5/5, valor da reserva em R$ 10,00, aluguéis 4/5 preservados sem alterações. Histórico do teste 6 mantido como devolvido, sem exclusão de registros e sem cobrança real. Evidências: docs/evidencias/aluguel-6-retirado.jpg e aluguel-6-devolvido.jpg.

Confirmada limitação da regra atual: administrador pode registrar retirada antes da data prevista; foi utilizada no teste. Não houve entrega física real, teste de atraso/multa ou nova disputa concorrente nesta etapa. Sem mudanças de código, esquema, execução de suíte ou commit/push; alteração documental local. Próxima implementação recomendada: manutenção do catálogo (editar informações/valores e gerenciar manutenção/inativação preservando reservas e histórico), Sol com raciocínio médio.

## Conferência visual da administração — 11/10/2026

Após o usuário reiniciar o backend e entrar como ADMIN, Administrar aluguéis carregou os registros reais 4 e 5 cancelados, com nome do usuário, kit, valores, datas e paginação. Conferida a organização em viewport de 390x844: cartões legíveis e sem transbordamento horizontal observado. Evidência em docs/evidencias/admin-alugueis-mobile.jpg; viewport restaurado. Isso não equivale a teste em aparelho físico nem valida transições.

Não havia reserva ativa; aberto Meus aluguéis na conta ADMIN e selecionado o kit TESTE existente. Usuário solicitado a preencher datas e criar uma nova reserva identificada para simular retirada/devolução, pois o preenchimento automático de datetime-local falhou anteriormente. Aguardando essa reserva; nenhum aluguel anterior foi alterado nesta conferência. Sem nova execução automatizada ou commit/push.

## Administração de retirada e devolução implementada — 11/10/2026

Implementados AdminRentals.tsx, admin-rentals.ts e CSS de cartões responsivos, com entrada Administrar aluguéis restrita a ADMIN em Home.tsx. Exibe usuário identificado por nome/ID, composição, total, datas previstas e efetivas, histórico e páginas de 20 registros. Retirada disponível para RESERVADO; devolução para RETIRADO/ATRASADO; confirmação explícita antes de operar. Reconfirma sessão/perfil, usa CSRF, bloqueia envio repetido e exige atualizar a lista após falha. Indicação de devolução vencida calculada no relógio de Brasília na renderização, sem alterar status persistido nem calcular multa automaticamente; atualização manual pela consulta.

AluguelController/AluguelService ganharam GET /alugueis/administracao?pagina=0, protegido pela autorização ADMIN existente na classe do serviço. DTO AlugueisAdminResponse mantém resposta previsível e acrescenta apenas o nome da conta, com busca em lote. Consulta ordenada por ID descendente, páginas inválidas rejeitadas. Retirada/devolução reaproveitam transições, datas e bloqueios existentes. Nenhuma mudança de esquema ou ddl-auto.

Verificações novas: 58 testes selecionados do backend (43 de segurança e 15 de estoque) passaram, com repositories/EntityManager simulados, incluindo consulta administrativa, privacidade da conta, paginação, CSRF e preservação das datas em transições repetidas. Os 34 testes Node passaram, incluindo troca de sessão/perfil, transições, CSRF, ausência de repetição após timeout e aviso de atraso. Build de produção e ESLint passaram; subprocessos dos testes Node e build exigiram execução fora da restrição do ambiente. Não houve novo fluxo de retirada/devolução no MariaDB nem conferência visual da tela administrativa: aba ainda estava na conta de cliente e o usuário foi solicitado a reiniciar o backend e entrar como admin. Próxima etapa é essa validação com reserva própria identificada; não utilizar aluguéis cancelados anteriores. Modelo recomendado: Sol médio. Tudo salvo localmente, sem novo commit/push.

## Reserva e cancelamento conferidos pela interface — 11/10/2026

Conferência realizada na conta de cliente já existente, com o kit 1 de teste, no MariaDB tech_help. A sessão inicial havia expirado; o usuário entrou novamente. O preenchimento automático dos campos datetime-local não funcionou; o usuário preencheu as datas e também confirmou a reserva pela interface antes da retomada. Reserva identificada como aluguel 5: usuário 1, kit 1, quantidade 1, diária R$ 5,00, retirada 12/10/2026 10:00 e devolução 14/10/2026 10:00, total R$ 10,00. Conferida tanto na tela quanto por SQL em leitura. Durante a reserva o kit ficou INDISPONIVEL e deixou de aparecer no catálogo disponível.

O agente cancelou exclusivamente o aluguel 5 pela interface, com revisão e confirmação. A tela mostrou Reserva cancelada e o aluguel Cancelado; SQL confirmou CANCELADO, kit DISPONIVEL e contadores da ferramenta preservados em 5/5. O aluguel anterior 4 continuou cancelado com R$ 20,00. Mantido o histórico do aluguel 5 cancelado, sem apagar registros. Evidência em docs/evidencias/aluguel-cancelado-5.jpg. Não houve alteração de código, esquema, commit/push nem nova execução de suíte automatizada nesta conferência. Este teste comprova o percurso básico desktop para uma reserva, não todos os erros, perfis ou concorrência. Próxima implementação: administração de retirada/devolução, recomendação Sol com raciocínio médio.

## Preferência de orientação sobre modelos — 11/10/2026

O usuário pediu que cada indicação de tarefa/próxima etapa inclua, em destaque e negrito, o modelo e o nível de raciocínio recomendados, para administrar seus créditos. Preferência registrada também em AGENTS.md; é orientação ao usuário, não troca automática do modelo. Para a próxima etapa, conferir reserva/cancelamento pela interface, recomendação atual: Sol com raciocínio médio. Astra médio fica recomendado para a revisão de segurança/concorrência. Ajustes estéticos continuam adiados. Esta atualização é documental, local, sem commit/push.

## Cabeçalho com atalhos visíveis — 11/10/2026

A pedido do usuário, substituída a regra que recolhia a navegação autenticada até 1650px. Home.tsx separa a navegação pública dos atalhos da conta em duas faixas no desktop; App.css mantém todos os atalhos permitidos pelo perfil visíveis acima de 960px, com quebra de linha quando necessária. Até 960px permanece o menu compacto. A altura real do cabeçalho é observada para ajustar destinos de rolagem e a seção Como funciona, inclusive ao redimensionar ou entrar/sair da conta. Não foram alteradas permissões ou regras de negócio.

Verificações desta alteração: TypeScript e build de produção passaram (Vite exigiu execução fora da restrição de subprocessos do ambiente); ESLint e git diff --check passaram. A aba aberta ainda mostrava a versão anterior; a tentativa de recarregar e inspecionar o resultado falhou por timeout do controle do navegador. Aparência final e navegação responsiva ainda precisam de conferência visual. Alterações salvas localmente, sem commit/push.

## Reservas integradas — retomada em 11/10/2026

O usuário criou a ferramenta e o kit de teste no catálogo. A inspeção identificou o kit vazio; uma unidade da ferramenta foi vinculada pela interface administrativa e confirmada em leitura no banco. Mantida a diária cadastrada pelo usuário (R$ 5,00). A conta administrativa foi configurada anteriormente com autorização explícita; não promover outras contas.

Implementados ReservarKitRequest, ReservaService e ReservaController: POST /alugueis/reservar cria aluguel e item na mesma transação READ_COMMITTED, reaproveitando estoque/permissões. Exige sessão do idUsuario informado, CSRF, quantidade positiva, retirada futura em Brasília e devolução posterior. Falha de inclusão desfaz o aluguel. Endpoints anteriores preservados; não houve alteração de esquema. A reserva inicial possui um kit, podendo pedir várias unidades conforme estoque.

RentalPage.tsx e rentals.ts integram consulta pública dos kits disponíveis, revisão de reserva, estimativa de diárias conforme regra existente, confirmação, Meus aluguéis e cancelamento antes da retirada. Todas as contas autenticadas podem alugar para si, conforme regra existente. Sessão é reconfirmada antes da escrita; não há repetição automática e falhas exigem atualizar/conferir aluguéis. Entrada pela Home e pelo menu. Retirada/devolução física no Tech Help; entrega, taxa e pagamento online seguem pendentes. Administração das retiradas/devoluções ainda não tem tela.

Verificações concluídas antes da interrupção: 31 testes Node; 59 testes de backend com BUILD SUCCESS, incluindo ReservaIntegracaoTests opt-in no MariaDB tech_help. Teste real comprovou rollback por estoque insuficiente, uma única reserva aceita entre dois kits concorrentes compartilhando uma ferramenta, valor persistido e cancelamento. Fixtures UUID próprias foram limpas; consulta posterior confirmou zero usuários/kits/ferramentas TESTE-AUTO restantes e preservação do kit do usuário. Esse cenário não representa teste de carga nem cobre todas as disputas possíveis.

Navegador: confirmada a composição administrativa e aberta a tela de aluguel com o kit real. Não foi enviada reserva pela interface. Detectado cabeçalho excedendo a largura; App.css agora usa menu recolhido para contas autenticadas até 1650px. Build de produção e ESLint foram executados novamente na retomada após o ajuste final do menu e passaram. O menu ajustado ainda não foi reconferido visualmente nesta retomada, pois a sessão anterior do navegador foi encerrada. Frontend estava desligado na retomada; backend e MariaDB com portas acessíveis. O processo antigo do backend precisa ser reiniciado para carregar o novo endpoint. Não presumir que o código compilado já esteja rodando.

Sem commit/push. Próxima etapa: conferir reserva/cancelamento pela interface com backend atualizado e depois integrar operações administrativas de retirada e devolução. Preservar os registros de teste criados pelo usuário.


## Catálogo administrativo integrado — 10/10/2026

Implementados AdminCatalog.tsx, catalog.ts e catalog.css, com entrada Administrar catálogo no menu de Home para contas ADMIN. Permite criar ferramentas e kits, listar ferramentas, selecionar kit, consultar composição, adicionar componentes e remover mediante confirmação. Reaproveita as APIs existentes; não altera código de produção do backend, permissões ou esquema. Não inclui edição/exclusão de ferramentas e kits, reservas pelo cliente nem administração de usuários.

Escritas reconfirmam identidade/perfil da sessão e usam CSRF, inclusive DELETE. A tela bloqueia cliques repetidos durante envio e exige atualização do catálogo após falhas, sem repetição automática. Quantidade total é apresentada como cadastro, não saldo livre. O cadastro público continua sem conceder ADMIN; nenhuma conta foi promovida ou criada por esta etapa.

Verificações: 55 testes selecionados de backend passaram (40 de segurança e 15 de estoque, todos com mocks), incluindo novos cenários administrativos. O teste de contexto/MariaDB não foi reexecutado nesta etapa. Os 29 testes Node e o build de produção passaram após executar fora da restrição de subprocessos que causou spawn EPERM. ESLint também terminou com sucesso na retomada após a interrupção. Não houve inspeção visual da tela em navegador nem escrita no banco real; roteiro manual no README do frontend. Alterações somente locais, junto do trabalho anterior, sem commit/push.

Próxima etapa: validar a tela com uma conta ADMIN e catálogo de teste identificado; em seguida integrar catálogo/reserva para o cliente, mantendo pendente a verificação de concorrência real do estoque. Entrega/taxa permanecem fora desta etapa.

## Estoque de aluguel — 10/10/2026

Implementado localmente: EstoqueAluguelService calcula o limite de reserva conforme a composição, a quantidade solicitada e as ferramentas comprometidas por todos os aluguéis RESERVADO/RETIRADO/ATRASADO. Preservada a exclusividade atual de um aluguel ativo por kit. Reservas futuras comprometem estoque imediatamente; não há calendário por intervalos. Kit vazio, componente em manutenção ou saldo insuficiente bloqueiam a reserva. /kits/disponiveis passa a filtrar pelo cálculo efetivo. Os contadores cadastrados não são reescritos; detalhes e limites estão em [ESTOQUE_ALUGUEL.md](ESTOQUE_ALUGUEL.md).

AluguelItemService e AluguelService recarregam/bloqueiam o aluguel antes das transições. Reserva bloqueia kit/ferramentas; composição do kit fica protegida durante aluguel ativo. Cancelamento/devolução/remoção não reativam kit INATIVO nem liberam um kit ainda comprometido em outro aluguel. Transações de alteração usam READ_COMMITTED; concorrência real entre transações ainda não foi testada.

Verificação nova: Maven test terminou com BUILD SUCCESS e 55 testes (15 de estoque com mocks, 38 de segurança com mocks e 2 de contexto/consultas com MariaDB real). Conexão confirmada com tech_help no MariaDB 10.4.32, ddl-auto=validate preservado e as novas consultas executadas em leitura. Consulta direta confirmou zero ferramentas, kits e aluguéis no momento da verificação. Nenhum registro ou tabela foi criado/alterado/removido. O Hibernate 7.4.1 registrou aviso de suporte mínimo ao MariaDB 10.6; testes passaram, mas a compatibilidade deve ser planejada, sem atualização automática do banco.

Frontend não foi alterado nem retestado nesta etapa. Trabalho anterior preservado na branch feat/home-grafite-assistente; nenhuma troca de branch, commit ou push. Próxima etapa recomendada: catálogo administrativo simples para cadastrar ferramentas e montar kits, aproveitando as APIs existentes, seguido de validação do fluxo real de reserva e concorrência antes da liberação para clientes.

## Agendamento e cancelamento — 10/10/2026

Implementação local: técnico responsável registra/altera data futura de serviço AGENDADO, no horário de Brasília; cliente ou técnico cancela antes do início, mediante confirmação. ADMIN mantém acesso pelo backend. Cancelar atualiza também o pedido para CANCELADA; não reabre oportunidades e mantém o histórico das propostas. Serviços iniciados/concluídos/cancelados não podem ser reagendados ou cancelados por essas operações. Não foi criada confirmação bilateral, calendário, validação de sobreposição ou exigência de agendamento para iniciar atendimentos existentes.

Adicionados AgendarServicoRequest e endpoints em ServicoController; ServicoService reutiliza os campos e estados existentes. Agendamento/cancelamento/início/conclusão atualizam a entidade sob bloqueio pessimista dentro da transação antes de verificar seu estado. EntityManager é simulado nos testes de segurança; concorrência real ainda precisa de verificação com MariaDB. O aceite de propostas continua com sua pendência de concorrência separada. ddl-auto=validate foi preservado; nenhum registro ou tabela foi alterado nesta etapa.

MyServices ganhou controles de data e confirmação de cancelamento, mensagens de resultado e tratamento de falhas; services.ts reconfirma sessão/permissões e envia CSRF. Datas digitadas são preservadas sem conversão automática de fuso pelo navegador. Os 38 testes de SegurancaApiTests e 24 testes Node passaram antes da interrupção. Na retomada, o relatório e os fontes foram conferidos e o build de produção passou. ESLint também terminou com sucesso nesta retomada, após execução lenta no ambiente. Os testes de backend usam EntityManager/repositories simulados; o fluxo no navegador e o bloqueio sob concorrência real no MariaDB não foram revalidados. Código e documentação permanecem locais, sem novo commit/push. Próxima etapa recomendada: revisar e corrigir o controle de disponibilidade/estoque do aluguel antes de integrar suas telas.


## Nomes dos técnicos — 10/10/2026

Implementado localmente: identificação por nome na comparação de propostas, confirmação do aceite e Meus serviços na visão do cliente. A proposta mantém o número como referência adicional; na ausência de nome, usa-se `Técnico #ID`. A identificação de clientes na visão do técnico permanece por número. Perfil profissional completo não foi implementado nesta etapa.

Backend: `TecnicoResumoResponse` e `GET /tecnicos/resumos?ids=...` retornam somente ID do técnico e nome de sua conta ativa. Aceita até 50 IDs positivos, elimina repetidos e busca técnicos/usuários em conjunto. O contrato dos endpoints anteriores foi preservado. Frontend: `technicians.ts` e `useTechnicianNames.ts` agrupam consultas, dividem lotes e preservam a identificação numérica após falhas; atualizar as listas renova os nomes. Nenhum dado de contato, documento ou acesso é retornado pelo novo endpoint.

Verificações novas: 33 testes de SegurancaApiTests passaram (repositories simulados), 21 testes Node passaram (transporte simulado), assim como build de produção e ESLint. Os testes não comprovam persistência no MariaDB nem inspeção visual no navegador. Testes acrescentados para resposta pública mínima, omissão de conta bloqueada, IDs inválidos/limite de lote e tratamento de indisponibilidade no frontend. Sem mudanças de banco/tabelas e sem novo fluxo em navegador/MariaDB nesta etapa. Alterações continuam locais, junto de Meus serviços e avaliações; não houve commit/push novo. Próxima etapa recomendada: agendamento e cancelamento simples, com regras de permissão e transição de estado, antes de avançar para aluguel.


## Avaliação integrada — concluída em 10/10/2026

Implementação local seguinte a Meus serviços: `ServiceReview.tsx` e `reviews.ts` permitem ao cliente consultar/enviar uma avaliação em cada serviço concluído, com nota inteira de 1 a 5 e comentário opcional de até 3000 caracteres. O técnico consulta a avaliação recebida no próprio atendimento. Avaliações existentes substituem o formulário. Edição/exclusão não fazem parte desta tela. A consulta só ocorre quando o usuário abre a avaliação, evitando uma requisição adicional para cada cartão ao carregar a lista.

Adicionado `POST /avaliacoes/servico/{idServico}/cliente`, com DTO próprio `AvaliarAtendimentoRequest` e autorização `avaliarComoCliente`: exige o cliente do serviço e deriva os IDs de usuário dos participantes no servidor. O endpoint anterior foi preservado. As regras existentes de conclusão, autoavaliação e repetição são reaproveitadas. O formulário reconfirma a sessão, usa CSRF, bloqueia envio repetido enquanto aguarda e exige consulta após erro, preservando o texto. Não há garantia nova de concorrência; os testes simulados não validam restrições do MariaDB.

Verificação final: 31 testes de SegurancaApiTests passaram após compilação limpa (147 fontes de produção), 19 testes Node passaram, assim como build de produção e ESLint. O cenário de nota fracionária detectou conversão para inteiro na primeira versão; o novo DTO valida o valor decimal antes de convertê-lo, rejeitando frações. A recompilação limpa eliminou classes antigas que o Maven havia reutilizado. Os testes usam repositories/transporte simulados; o teste de contexto com MariaDB não foi reexecutado. Nenhum banco/tabela foi alterado e não foi executado novo fluxo no navegador com banco real. Meus serviços e avaliação estão salvos localmente, sem novo commit/push; o checkpoint anterior confirmado no GitHub continua sendo `e45b296`. Próxima etapa funcional recomendada: identificar técnicos por nome nas propostas e atendimentos, com dados públicos mínimos, preservando informações pessoais.


## Meus serviços — 09/10/2026

O usuário autorizou seguir a ordem recomendada: preservar o trabalho anterior em commit/GitHub e integrar acompanhamento, início e conclusão antes da avaliação. O checkpoint `e45b2966804c94183a5fc36f51aad50fe258c050` foi criado e enviado à branch `feat/home-grafite-assistente`; o hash foi confirmado diretamente com `git ls-remote`. Inclui login/publicação, propostas, adaptação à prévia aprovada e documentação anterior. As observações históricas abaixo sobre essas mudanças estarem somente locais foram superadas por esse checkpoint.

Implementado depois do checkpoint, ainda somente local: `features/servicos/MyServices.tsx` e `services.ts`, acesso pelo menu em `Home.tsx` e atualização dos avisos de propostas/Home. Cliente consulta seus atendimentos; técnico responsável confirma início e conclusão; contas com ambos os perfis alternam as listas. Exibe valor, proposta, identificador da outra pessoa e datas. `AGENDADO` aparece como “Aguardando início”, pois o aceite ainda não preenche data combinada. Não foi criado agendamento, pagamento ou avaliação nesta etapa.

A escrita reconfirma a identidade da sessão e obtém CSRF pelo cliente HTTP existente. Cliques repetidos são bloqueados durante o envio na tela; após falha é necessário atualizar a lista antes de tentar novamente. Não há repetição automática de POST. Isso não resolve os riscos existentes de transações simultâneas ou de troca de sessão entre requisições; a autorização definitiva continua no backend. A consulta usa os endpoints existentes por cliente/técnico. Não foi necessário alterar código de produção do backend nem o esquema.

Verificação nova: 28 testes de `SegurancaApiTests` passaram, usando repositories simulados, incluindo isolamento das listas/detalhes, rejeição de execução por outro técnico, CSRF, ordem de início/conclusão e preservação das datas após tentativas repetidas. Os 17 testes Node passaram, cobrindo também permissões de apresentação, troca de sessão e ausência de repetição de escrita após timeout. Build de produção e análise ESLint do frontend passaram.

MariaDB e servidores da aplicação não estavam ouvindo nas portas locais verificadas (3306, 8080, 8082 e 5173). Não foi reexecutado o teste de contexto com banco nem um novo fluxo no navegador. Nenhum registro de negócio foi criado/alterado/removido por esta etapa. Roteiro manual de conferência está no README do frontend. Próxima implementação: avaliação do atendimento concluído pelo cliente, seguida da identificação básica de técnicos. Ajustes estéticos e símbolo continuam adiados.


## Prioridade definida pelo usuário — 08/10/2026

Priorizar a conclusão funcional do MVP. Adiar ajustes estéticos da Home e criação do símbolo. Ideias discutidas para depois: manter “Qual parte precisa de ajuda?” como entrada principal; remover a repetição de categorias sob o notebook e o convite final equivalente; manter retomada de rascunho quando existir. Para identidade, explorar um monograma TH com detalhe de conexão; conceito ainda não desenhado nem escolhido definitivamente. Não preencher espaços com seções sem utilidade.

Próxima etapa funcional recomendada: integrar acompanhamento, início e conclusão dos serviços às telas, aproveitando as operações existentes no backend, e depois integrar avaliação. Aluguel ainda exige correção de estoque, catálogo e administração; entrega/taxa permanece parte da visão original e precisa de implementação ou adiamento explícito no escopo. Esta atualização é documental, sem alteração de código, execução de testes ou commit/push.

## Referência visual corrigida — prévia integrada enviada pelo usuário

O usuário esclareceu que o frontend da auditoria não era sua versão visual final. A referência aprovada passou a ser `TechHelp_Previa_Integrada (3).html`, fornecida da pasta Downloads. Uma cópia integral está em [referencias/TechHelp_Previa_Integrada.html](referencias/TechHelp_Previa_Integrada.html), SHA-256 `CA2A5F77E36D06EDF8862E83A3CA07D71F5FC8B89AE3710F8F355E219C95938F`. Esse arquivo é uma referência histórica de design: contém catálogo simulado e avisos antigos de funcionalidades indisponíveis, não representa o estado funcional atual.

A interface React foi adaptada à referência: cabeçalho fixo, notebook em módulos animados no canvas, entrada por categorias, explorador de serviços com SVGs, sequência de quatro passos ligada à rolagem, novas seções de aluguel/profissionais e rodapé. `features/home/Landing.tsx` integra a apresentação; `preview.css`, `moduleField.js` e `illustrations.js` preservam estilos e desenhos da prévia. Os módulos JavaScript visuais têm declarações de tipos para uso pelo React/TypeScript. Não foi adicionada dependência.

`Home.tsx` mantém sessão, logout, publicação, rascunho, Meus pedidos e Área do técnico. Categorias vêm da API; o catálogo simulado da prévia não foi incorporado. O CTA profissional abre cadastro com técnico selecionado ou a área do técnico autenticado. O assistente usa a trilha e o painel da nova identidade; autenticação e propostas seguem integradas. Avisos de prévia sem envio foram retirados, mas aluguel, perfil completo e acompanhamento ainda são identificados como pendentes.

Verificações antes da interrupção: 14 testes Node, lint e build passaram, após executar fora da restrição que causava `spawn EPERM`. Navegador: Home com categorias reais, canvas, assistente e perguntas condicionais, retorno com rascunho, cadastro com campos de técnico, menu mobile e diálogo de login. Layout conferido no desktop e em viewport de 390 × 844, sem excesso horizontal na Home. O navegador não permitiu abrir o HTML por `file://`; a referência foi lida como código e a aplicação React foi inspecionada em localhost. Não afirmar comparação visual pixel a pixel ou novo teste completo de publicação/aceite nesta adaptação.

Nenhum banco ou backend foi alterado nesta etapa visual. As verificações no navegador não criaram contas nem registros de negócio. Tudo permanece salvo localmente, sem novo commit/push. Validar o resultado visual com o usuário antes de avançar para acompanhamento do serviço.

Na retomada após o limite de créditos, foi ajustado o cancelamento da celebração em `Landing.tsx` ao pausar movimento, ativar redução de movimento ou sair da Home. O link de pular para o conteúdo também foi colocado acima do cabeçalho fixo. Lint e build foram executados novamente e passaram. A referência e o código estavam preservados; não foi necessário refazer a integração.

## Atualização mais recente — propostas em 26/09/2026

Implementadas localmente a Área do técnico (`TechnicianBoard.tsx`) e a comparação/aceite pelo cliente (`ClientProposals.tsx`, acessível em Meus pedidos). O técnico consulta oportunidades, envia valor, condições, prazo e disponibilidade e acompanha suas propostas. O cliente compara e confirma o aceite, sem pagamento. As telas consultam a identidade da sessão antes das escritas, bloqueiam cliques repetidos e exigem atualização após resultado incerto.

No backend, a lista de oportunidades inclui ABERTA e EM_NEGOCIACAO: receber a primeira proposta não deve ocultar o pedido dos demais técnicos. A criação de proposta é transacional, incluindo a mudança do pedido para negociação. O aceite verifica se o pedido ainda permite negociação, cria o serviço AGENDADO e recusa outras propostas ENVIADA. Essas verificações não garantem exclusão entre transações concorrentes; controle de concorrência permanece pendente.

Antes da interrupção, passaram 26 testes Maven (25 de segurança/fluxo com repositories simulados e um de contexto), 14 testes Node, build e lint do frontend. Os relatórios Maven foram relidos nesta retomada; isso não representa uma nova execução da suíte. O envio de proposta pelo técnico foi validado no navegador com o backend apontado para `tech_help_teste`.

Retomada: servidores anteriores estavam desligados. MariaDB, backend em 8082 e Vite em 5173 foram iniciados para o teste. O log confirmou `tech_help_teste` e validação JPA. No navegador, o cliente comparou duas propostas, cancelou a confirmação e depois aceitou a de R$ 150,50. Após atualização, a interface e uma consulta SQL confirmaram pedido CONTRATADA, proposta 8 ACEITA, proposta 7 RECUSADA e um único serviço 6 AGENDADO. A conta do técnico confirmou a proposta aceita e a retirada do pedido das oportunidades. Logout verificado.

Foram removidos somente os próprios registros temporários, por IDs e identificadores conferidos: serviço 6, propostas 7/8, solicitação 7, cliente 7, técnicos 6/7, usuários 12/13/14 e seus vínculos de perfil. Os totais retornaram a 7 usuários, 4 solicitações, 5 propostas e 4 serviços. Nenhuma alteração de esquema ou escrita de negócio em `tech_help`; contadores automáticos não foram redefinidos. Permanece o aviso do Hibernate sobre MariaDB 10.4.32 abaixo do mínimo 10.6 suportado.

Limites: técnicos ainda aparecem por número na comparação; perfil público completo, execução, conclusão e avaliação não estão integrados às telas. Próxima etapa funcional: acompanhamento e execução do serviço, seguida da avaliação. Alterações de login/publicação e propostas permanecem locais, sem novo commit/push nesta retomada.

## Atualização mais recente — integração do frontend em 25/09/2026

Após os commits `f34647d` (documentação) e `348d369` (segurança do backend), confirmados no GitHub em `feat/home-grafite-assistente`, foi implementada localmente a integração de cadastro, login e publicação. Esta seção substitui as observações históricas abaixo que descrevem Entrar/Publicar como indisponíveis.

- `AuthDialog.tsx` cadastra clientes/técnicos e faz login; `api.ts` envia cookies, obtém CSRF antes das escritas e distingue ausência de sessão de falha de conexão.
- `Home.tsx` restaura a sessão, permite logout, conserva o assistente durante login e publica usando o cliente consultado em `/auth/me`. Conta de técnico sem perfil CLIENTE não publica pedidos.
- `publication.ts` valida e converte respostas em título/descrição, atendimento e urgência. Cidade e bairro entram na descrição presencial, sem inventar endereço cadastrado. `MyRequests.tsx` consulta os pedidos da conta.
- Rascunho mantido em erros e durante autenticação; removido somente após sucesso. Bloqueio de cliques repetidos e conferência dos pedidos após envio sem confirmação. Não existe idempotência no servidor, portanto não há garantia absoluta contra duplicação após falhas de rede/recarregamento.
- Verificações: 11 testes Node passaram, build de produção e ESLint passaram. Testes automatizados cobrem regras do rascunho, conversão da publicação e transporte HTTP simulado; não são uma suíte automatizada de navegador.
- Validação manual no navegador com frontend em 5173 e backend temporário em 8082 apontado explicitamente para `tech_help_teste`: cadastro de cliente e técnico, senha incorreta, login, rascunho preservado, publicação real, Meus pedidos, sessão restaurada ao recarregar, rascunho limpo após sucesso, logout e bloqueio de publicação por técnico. Revisão visual em largura de celular e desktop.
- Foram criados apenas dois usuários e uma solicitação temporários identificados. Após remoção dos próprios registros, os totais retornaram a 7 usuários e 4 solicitações. Dados preexistentes e esquema preservados; `tech_help` não foi usado para escrita. IDs automáticos podem apresentar saltos devido aos testes.

Detalhes e comandos estão no [README do frontend](../frontend/README.md). Nenhuma dependência nova foi adicionada. As alterações desta etapa do frontend ainda não foram incluídas em commit/push. Próxima etapa recomendada: área do técnico para consultar oportunidades e enviar propostas, seguida da comparação/aceite pelo cliente. Recuperação de senha, verificação de e-mail, estoque e demais pendências históricas permanecem abertas.

## Atualização de desenvolvimento — 25/09/2026

A autenticação por sessão e a autorização do backend foram implementadas localmente, após a etapa documental abaixo. Este registro mais recente substitui as afirmações históricas de ausência de autenticação. Detalhes de endpoints, CSRF, permissões, arquivos e limites estão em [AUTENTICACAO.md](AUTENTICACAO.md).

Foram adicionados Spring Security, `SecurityConfig`, `AutenticacaoService`, `AcessoService`, `AuthController`, `SessaoResponse` e testes de segurança. Os services existentes receberam verificações de acesso; cadastro continua com BCrypt; respostas JSON não expõem o hash da senha. A habilitação explícita de segurança web corrigiu a falta de `HttpSecurity` no contexto de teste.

O frontend não foi integrado nesta etapa. A próxima implementação é conectar cadastro/login e publicação de solicitações à identidade da sessão. Estoque, validações de tamanho, datas de criação e riscos de concorrência da auditoria continuam pendentes. Nenhuma alteração de esquema foi necessária; `ddl-auto=validate` foi preservado.

Branch mantida: `feat/home-grafite-assistente`. A validação descrita abaixo ocorreu antes do commit e do push desta implementação. O usuário autorizou posteriormente registrar a documentação e a autenticação em dois commits e enviá-los nessa branch. Consultar o histórico Git e a referência remota para confirmar a publicação, sem interpretar os estados locais históricos deste documento como permanentes.

### Verificações executadas nesta retomada

- Compilação dos 146 arquivos de produção e execução Maven: **23 testes passaram, sem falhas ou erros** (22 de segurança/fluxo com repositories simulados e um de contexto com MariaDB).
- O MariaDB estava parado inicialmente. Após iniciar a instalação existente do XAMPP, conexão com `tech_help`, detecção dos 28 repositories e validação JPA passaram. Nenhum registro de negócio foi criado, alterado ou removido nesta retomada.
- Backend iniciado temporariamente na porta 8082: `/categorias` retornou 200; `/auth/me` e `/solicitacoes/abertas` sem sessão retornaram 401; `/auth/csrf` retornou 200 com token e cookie HttpOnly/SameSite=Lax; login sem CSRF retornou 403; login com CSRF e credenciais inexistentes retornou 401.
- Login válido e fluxo autenticado foram verificados na suíte com repositories simulados, não com criação de contas no banco real. O frontend não foi alterado nem retestado nesta etapa.
- Permanece o aviso de compatibilidade: MariaDB 10.4.32 está abaixo do mínimo 10.6 informado pelo Hibernate, embora conexão e validações desta execução tenham passado.

## Registro histórico da auditoria e continuidade

**Data do registro e da reconferência documental: 23/09/2026.**

Este documento preserva decisões do usuário, a auditoria técnica executada anteriormente nesta conversa em 23/09/2026 e uma nova conferência de continuidade. Seu objetivo é permitir a retomada do projeto sem depender apenas do histórico de conversa.

O usuário manifestou preocupação de que parte do trabalho pudesse estar somente em conversas, arquivos locais, outra branch ou no banco do computador. Não foi comprovada perda de código. A pasta auditada estava limpa no Git, mas faltava documentação versionada da auditoria e de parte das decisões.

## 1. Como interpretar este registro

Há quatro tipos de informação:

- **Decisão do produto:** intenção declarada pelo usuário; não implica implementação.
- **Resultado da auditoria anterior:** execução ou observação registrada em 23/09/2026; não equivale a nova execução nesta etapa documental.
- **Reconferência atual:** leitura de arquivos, inspeção do Git e consulta remota realizadas para produzir estes documentos.
- **Pendente ou não confirmado:** trabalho futuro, risco ainda não reproduzido ou informação sem evidência suficiente.

Atualizar os itens relevantes quando houver mudanças. Não tratar contagens de registros, processos ativos ou referências de branches como fatos permanentes.

## 2. Produto e filosofia do MVP

O Tech Help conecta clientes que precisam de serviços de informática a técnicos que podem enviar propostas e realizar atendimentos. Exemplos: manutenção, formatação, instalação de programas, configuração, redes e suporte técnico.

Fluxo desejado: solicitação do cliente → propostas dos técnicos → escolha/aceite → execução → conclusão → avaliação com nota e comentário. O código atual permite avaliações nos dois sentidos, entre cliente e técnico; entender essa regra antes de modificá-la.

As ferramentas e equipamentos de aluguel pertencem ao próprio Tech Help. O produto deve oferecer catálogo, disponibilidade, valores, solicitação de aluguel, retirada e futura entrega com taxa conforme distância/local. Deve haver administração do catálogo e do estoque. Entrega, taxa e administração completa são objetivos, não funcionalidades concluídas.

O projeto é acadêmico, mas deve ser organizado e funcional. Manter o MVP simples, sem IA dentro do produto, recomendação sofisticada, arquitetura exagerada ou funcionalidades sem utilidade clara. Preservar a estrutura quando razoável e corrigir incrementalmente.

O usuário tem conhecimento básico/intermediário de Java, orientação a objetos e SQL, e está aprendendo Spring Boot, REST, JPA, Maven, React e TypeScript. Quer entender problemas e decisões: explicar mudanças importantes, indicar arquivos e razões, implementar e verificar. Evitar respostas que omitam o estado dos testes, do Git ou do banco.

## 3. Reconferência atual e GitHub

Pasta local: `C:\Users\Miguel Rodrigues\Desktop\Tech-Help`.

Repositório: [miguelrodriguees/Tech-Help](https://github.com/miguelrodriguees/Tech-Help).

| Referência | Commit confirmado localmente e por consulta ao GitHub |
| --- | --- |
| Branch atual `feat/home-grafite-assistente` | `a814eb8423b825c56f4ffd171a7c88a42aed0d8f` |
| `main` | `0d99b83ee077f85c06bb5135d24713732e34ac26` |

Os dois commits adicionais do frontend são:

1. `b35bde9` — Implementa Home grafite e assistente de solicitação com rascunho.
2. `a814eb8` — Organiza seções da Home e destaca aluguel de ferramentas.

A Home e o assistente mais recentes estão na [branch de frontend](https://github.com/miguelrodriguees/Tech-Help/tree/feat/home-grafite-assistente). Começar pela `main` nesse estado apresenta uma interface anterior; isso não significa que o trabalho foi perdido.

### O que foi reconfirmado nesta etapa

- A pasta estava sem alterações locais pendentes antes da criação dos documentos.
- Branch, commits e os dois commits adicionais permaneciam iguais aos informados no relatório.
- `git ls-remote` confirmou as duas branches diretamente no GitHub. A primeira tentativa foi bloqueada pela conectividade do sandbox; a consulta autorizada fora dele funcionou.
- Não havia diferenças nos arquivos versionados de `backend/`, `frontend/` e `database/` em relação a `a814eb8`.
- Foram relidos README, configurações, dependências, teste de contexto e trechos relevantes de cadastro, perfis, aluguel e frontend. As contagens por camada também foram reconferidas.
- Os achados continuam compatíveis com o código: BCrypt no cadastro, ausência de camada de autenticação identificada, vínculo de perfil sem validação de identidade, aluguel sem reserva do estoque das ferramentas e publicação do frontend ainda informativa.
- Não existiam `AGENTS.md` nem este documento. A documentação anterior estava concentrada no README da raiz e no README do frontend.

### Diferenças em relação à auditoria fornecida

Não foi encontrada mudança de código ou de branch que contradiga o relatório. A mudança desta etapa é a criação de documentação de continuidade. Não houve implementação de correções, troca de branch, merge, commit ou push.

Os novos documentos foram criados somente no diretório de trabalho. No encerramento desta etapa estão pendentes de inclusão em commit e envio ao GitHub. Esse estado deve ser atualizado quando forem efetivamente registrados e publicados.

Não foram repetidos build, testes da aplicação, testes HTTP ou consultas ao MariaDB nesta etapa documental. O estado atual dos processos, os totais atuais do banco e a integridade do backup não foram reconfirmados.

## 4. Tecnologias e configuração

| Parte | Configuração ou versão |
| --- | --- |
| Backend | Java 25; Spring Boot 4.1.0; Web MVC; Data JPA; Bean Validation; Maven Wrapper |
| Runtime observado na auditoria | Java 25.0.4; Hibernate 7.4.1.Final |
| Banco observado na auditoria | MariaDB 10.4.32 pelo XAMPP, porta 3306 |
| Frontend | React, TypeScript, Vite e Axios; Node 24.18.0 observado na auditoria |
| Backend HTTP | Porta usual 8080 |
| Frontend/CORS | Origem permitida `http://localhost:5173` |
| Cliente HTTP | API padrão `http://localhost:8080`; alternativa por `VITE_API_URL`; timeout de 8 segundos |
| IDE anterior | NetBeans; `backend/nbactions.xml` preservado |

O [pom.xml](../backend/pom.xml) define Java 25 e Spring Boot 4.1.0. `spring-security-crypto` fornece BCrypt, não uma camada de autenticação das rotas.

[application.properties](../backend/src/main/resources/application.properties) aponta para `jdbc:mariadb://127.0.0.1:3306/tech_help`, com usuário local `root`, senha vazia e `spring.jpa.hibernate.ddl-auto=validate`. SQL e formatação de SQL estão habilitados. Essa configuração local não deve ser confundida com configuração pronta para produção.

O Maven global pode não estar no PATH; utilizar o Wrapper. O [README do frontend](../frontend/README.md) contém instruções de execução. Conferir o runtime disponível, especialmente em outro computador ou ambiente remoto: o XAMPP local não acompanha o repositório.

## 5. Estrutura e entidades

| Pasta | Função |
| --- | --- |
| `backend/` | Backend verdadeiro; projeto Maven e aplicação Spring Boot |
| `frontend/` | Interface React/TypeScript e testes das regras do formulário |
| `database/` | Script de instalação inicial, dados iniciais e views |

Contagens do backend: **28 entidades, 28 repositories, 27 services, 27 controllers e 23 DTOs**. `model/` tem 33 arquivos: 28 entidades e cinco classes de chaves compostas. Existem uma configuração web e um tratamento global de exceções.

| Área | Entidades |
| --- | --- |
| Contas | Usuario, Perfil, UsuarioPerfil, Cliente, Tecnico, Endereco |
| Perfil profissional | Categoria, Especialidade, TecnicoEspecialidade, Portfolio, Certificacao |
| Serviços | Solicitacao, SolicitacaoAnexo, Proposta, Servico, Avaliacao |
| Conversas | Conversa, ParticipanteConversa, Mensagem |
| Relacionamento | Favorito, Denuncia, Notificacao |
| Aluguel | Ferramenta, Kit, KitFerramenta, Aluguel, AluguelItem |
| Pagamento | Pagamento |

A estrutura `controller → service → repository` é adequada para continuar. Os relacionamentos são representados principalmente por IDs no Java, apoiados por restrições no banco. Não há justificativa para substituir toda a modelagem apenas por preferência arquitetural.

Na auditoria anterior não foram encontrados outro backend, arquivos-fonte vazios, duplicatas exatas nem marcações TODO/FIXME nas buscas realizadas. Imagens antigas e `react-router-dom` pareceram não utilizados; conferir referências antes de removê-los. Ausência de TODO não significa conclusão.

## 6. Resultados da auditoria anterior — não reexecutados nesta etapa

| Verificação em 23/09/2026 | Resultado anterior |
| --- | --- |
| Compilação Maven | Passou |
| Teste `contextLoads` | Falhou com MariaDB parado; passou após iniciá-lo |
| Inicialização do backend | Funcionou em 8080; detectou 28 repositories; validação JPA passou |
| Testes do frontend | Seis passaram |
| Build do frontend | Passou |
| ESLint | Passou |

O frontend teve inicialmente `spawn EPERM` no sandbox. A execução autorizada fora dele passou; o bloqueio não foi classificado como defeito do código.

O erro inicial do backend foi conexão recusada com `127.0.0.1:3306`; a mensagem sobre determinação do Dialect foi consequência da falta de conexão. Iniciar o MariaDB existente resolveu, sem mudar o Dialect.

### Verificações HTTP anteriores

Foram consultados categorias, solicitações abertas, ferramentas e kits. Em `tech_help_teste`, foram criados cliente e técnico temporários e exercitado o fluxo solicitação → proposta → aceite → início → conclusão → avaliação com nota e comentário.

Funcionaram os bloqueios de proposta duplicada, conclusão antes do início, avaliação antes da conclusão, avaliação duplicada e nota acima de cinco. Também foram exercitadas criação de ferramenta/kit, associação ao kit, aluguel, inclusão de item, retirada e devolução.

**As operações foram aceitas sem autenticação.** A comprovação de fluxo funcional não comprova segurança das permissões.

Houve uma instância em 8080 usando `tech_help` e uma temporária em 8081 usando `tech_help_teste`. A de 8081 foi encerrada. Ao fim da auditoria, MariaDB e backend em 8080 foram deixados ativos; não presumir que continuam ativos em outra tarefa.

Não houve teste de carga/concorrência nem validação visual completa em navegador. As chamadas HTTP não foram adicionadas como suíte permanente ao repositório.

## 7. Problemas reproduzidos e ainda pendentes

Os resultados HTTP abaixo pertencem à auditoria anterior. O código relevante permanece igual na reconferência documental; não foram implementadas correções.

| Problema reproduzido | Evidência anterior | Arquivos para investigar |
| --- | --- | --- |
| Identidade controlada pelo chamador, sem autenticação | Solicitação criada com `idCliente` informado e fluxo completo executado sem login | [SolicitacaoService](../backend/src/main/java/br/com/techhelp/service/SolicitacaoService.java), [CadastroService](../backend/src/main/java/br/com/techhelp/service/CadastroService.java), [WebConfig](../backend/src/main/java/br/com/techhelp/config/WebConfig.java), pom.xml |
| Atribuição de ADMIN sem autorização | `POST /perfis/vinculos` retornou 201 ao atribuir ADMIN ao usuário temporário sem login | [PerfilController](../backend/src/main/java/br/com/techhelp/controller/PerfilController.java), [PerfilService](../backend/src/main/java/br/com/techhelp/service/PerfilService.java) |
| Estoque inconsistente no aluguel | Ferramenta com estoque 1 em um kit; aluguel aceitou quantidade 100; disponibilidade da ferramenta continuou 1 | [AluguelItemService](../backend/src/main/java/br/com/techhelp/service/AluguelItemService.java), [KitFerramentaService](../backend/src/main/java/br/com/techhelp/service/KitFerramentaService.java), [AluguelService](../backend/src/main/java/br/com/techhelp/service/AluguelService.java) |
| Entrada inválida vira erro interno | Nome de 121 caracteres retornou HTTP 500; coluna aceita 120, sem limite correspondente no DTO | [CadastrarClienteRequest](../backend/src/main/java/br/com/techhelp/dto/CadastrarClienteRequest.java), [CadastrarTecnicoRequest](../backend/src/main/java/br/com/techhelp/dto/CadastrarTecnicoRequest.java), [Usuario](../backend/src/main/java/br/com/techhelp/model/Usuario.java), [ApiExceptionHandler](../backend/src/main/java/br/com/techhelp/exception/ApiExceptionHandler.java) |
| Data gerada no banco não retornada imediatamente | `dataCadastro` vazia na resposta de criação de solicitação | [Solicitacao](../backend/src/main/java/br/com/techhelp/model/Solicitacao.java), SolicitacaoService |

A última observação não comprova ausência da data na tabela: o problema observado foi a resposta da criação.

### Divergência sobre login no histórico

O usuário relatou um teste anterior em que a solicitação foi bloqueada antes do login e funcionou após entrar. A versão auditada não contém autenticação identificável e aceitou operações sem login. Não foi comprovado se o relato corresponde a outra pasta, versão, ambiente ou validação. Preservar essa divergência; não inventar explicação e não considerar login pronto sem localizar e testar sua implementação.

## 8. Riscos por leitura e melhorias — sem reprodução de concorrência

- [PropostaService.criar](../backend/src/main/java/br/com/techhelp/service/PropostaService.java) salva proposta e altera solicitação sem transação envolvendo ambas as operações: risco de atualização parcial.
- Aceite de propostas e reserva de kits não têm proteção explícita contra concorrência. Isso exige investigação/testes próprios; não foi reproduzido por teste simultâneo.
- Respostas retornam majoritariamente entidades diretamente, listagens não possuem paginação e estados usam strings espalhadas pelo código. São pontos de manutenção a avaliar incrementalmente.
- Alguns tamanhos de colunas e mapeamentos diferem. Exemplo observado: [Pagamento.gateway](../backend/src/main/java/br/com/techhelp/model/Pagamento.java) declara 80 no Java; SQL e banco consultado aceitam 50.

## 9. Funcionalidades iniciadas e limites

| Área | Estado e trabalho pendente |
| --- | --- |
| Contas | Cadastro com BCrypt existe; login, identidade autenticada e autorização estão ausentes |
| Serviços | Fluxo básico existe no backend; faltam integração e telas de propostas, acompanhamento e avaliações |
| Aluguel | Ferramentas, kits, reserva, retirada e devolução básicos; corrigir estoque e completar administração/catálogo |
| Entrega | Escolha, endereço, distância e cálculo de taxa não encontrados |
| Pagamento | [PagamentoService](../backend/src/main/java/br/com/techhelp/service/PagamentoService.java) marca PAGO com gateway SIMULADO; não há integração financeira real comprovada |
| Anexos | URL e metadados; sem fluxo completo de upload |
| Notificações | Endpoints existentes; geração automática ligada ao fluxo principal não encontrada |
| Verificação profissional | Campos de status existentes; fluxo administrativo incompleto |
| Agendamento/cancelamento | Alguns campos e estados existem sem operações completas correspondentes |

## 10. Frontend preservado

A Home possui identidade grafite, off-white e laranja e consulta real de categorias com carregamento, falha/repetição e catálogo vazio. O assistente atende Hardware, Redes e Software, com perguntas condicionais, restrições de remoto, revisão, edição e rascunho em `sessionStorage`. Existem cuidados de navegação mobile, teclado e movimento no código; não equivalem a validação visual completa.

Arquivos centrais:

- [Home.tsx](../frontend/src/pages/Home.tsx): categorias, navegação, armazenamento e mensagens de funcionalidades futuras.
- [RequestAssistant.tsx](../frontend/src/features/solicitacao/RequestAssistant.tsx): quatro etapas e revisão.
- [flow.ts](../frontend/src/features/solicitacao/flow.ts): regras condicionais e leitura do rascunho.
- [api.ts](../frontend/src/services/api.ts): configuração HTTP.
- [App.css](../frontend/src/App.css): visual.
- [request-flow.test.mjs](../frontend/tests/request-flow.test.mjs): seis testes de regras.

Entrar e Publicar apenas informam que as funções ainda não estão disponíveis. Não existe envio real da solicitação nessa interface. As demais áreas são principalmente explicações do produto planejado. A integração atual concentra-se na leitura de categorias, sem pessoas, avaliações, preços ou estoque fictícios usados para simular funcionalidades prontas.

## 11. Banco, backup e dados locais

Os seguintes números são da auditoria anterior, não de uma nova consulta nesta etapa:

| Banco | Estrutura e dados observados |
| --- | --- |
| `tech_help` | 28 tabelas, duas views, três perfis, seis categorias e 17 especialidades; usuários, solicitações, serviços e avaliações vazios |
| `tech_help_teste` | 28 tabelas e duas views; sete usuários, quatro serviços concluídos e quatro avaliações preexistentes |

No principal foram contadas 28 chaves primárias, 40 estrangeiras, 46 CHECKs e 15 restrições UNIQUE. Views: `vw_tecnico_resumo` e `vw_solicitacao_resumo`.

O Hibernate registrou que a versão mínima suportada pelo MariaDBDialect utilizado é 10.6; o MariaDB 10.4.32 conectou e passou nas verificações realizadas, mas está fora da faixa informada. Isso não é prova de falha em todas as operações nem motivo para uma atualização improvisada.

[techhelp_v2_mariadb_limpo.sql](../database/techhelp_v2_mariadb_limpo.sql) cria estrutura e dados iniciais. É um script de instalação inicial, não um sistema de migrações nem um backup dos registros atuais. Não foi importado sobre os bancos na auditoria.

Os dados reais ficam no armazenamento do XAMPP, fora da pasta versionada. O usuário informou ter feito um backup completo antes de atualizar o Windows. **Localização e integridade desse backup não foram verificadas.** Não afirmar que os dados estão no GitHub nem sobrescrever `tech_help_teste` como se fosse descartável.

Os registros temporários criados na auditoria foram removidos, preservando os dados anteriores. Contadores automáticos de IDs não foram restaurados; saltos de numeração podem resultar dos testes e não indicam perda de registros.

## 12. Testes existentes e execução futura

- [TechhelpBackendApplicationTests.java](../backend/src/test/java/br/com/techhelp/TechhelpBackendApplicationTests.java): um `contextLoads` com `@SpringBootTest`; não cobre regras de negócio. Sem perfil separado encontrado, usa a configuração padrão e depende do banco configurado.
- [request-flow.test.mjs](../frontend/tests/request-flow.test.mjs): seis testes de regras condicionais, restrições de remoto, edição/invalidação de respostas e serialização/restauração do rascunho. Não são testes de navegador nem do fluxo integrado.
- Os comandos HTTP da auditoria foram pontuais; ainda faltam testes permanentes de autenticação, autorização, fluxo de serviço, estoque e concorrência.

Comandos básicos estão em [AGENTS.md](../AGENTS.md). Antes de testes com escrita, conferir o banco efetivo e isolar os próprios registros. Nesta etapa exclusivamente documental, a verificação apropriada é conferir conteúdo, referências de arquivos e escopo das alterações; não é necessário repetir os testes da aplicação.

## 13. O que estava fora do histórico versionado

Antes destes documentos, o relatório, parte das decisões de produto e as preferências didáticas estavam principalmente na conversa. O anexo fornecido ao agente ficava fora do repositório. Salvá-lo na conversa não o colocava automaticamente no GitHub.

Na auditoria/conferência anteriores foram encontrados em `backend/target/`: `audit-startup.log`, `audit-startup-live.log`, `audit-startup-testdb.log`, `audit-tests.log` e `audit-fixtures.json`. São artefatos locais ignorados pelo Git, sujeitos a limpeza do build. O JSON guarda IDs temporários usados na limpeza; não é backup nem requisito de execução. Sua existência futura não é garantida.

Também foram encontrados como ignorados `backend/HELP.md` (referências genéricas), `frontend/node_modules/` e `frontend/dist/`. Dependências e resultados de build não precisam ser versionados para preservar o código-fonte. Processos ativos e conteúdo dos bancos também não são preservados por commit dos arquivos do projeto.

A conferência não investigou todos os diretórios do computador, outros repositórios, todas as referências remotas ou conversas não fornecidas. Não garante preservação de todo o histórico externo; tampouco encontrou evidência de código perdido na pasta auditada.

## 14. Próximas etapas e critérios de continuidade

1. Implementar autenticação e autorização mínima no backend, aproveitando cadastro e BCrypt.
2. Automatizar o fluxo de serviço e as permissões em conjunto com essa implementação.
3. Integrar login e publicação no frontend.
4. Implementar telas de propostas, acompanhamento e avaliação.
5. Corrigir estoque e consolidar aluguel com retirada.
6. Implementar entrega e cálculo de taxa.
7. Completar administração e demais recursos úteis ao MVP.

Validações de entrada e consistência devem acompanhar cada etapa. Evitar realizar todas as pendências em uma única mudança.

**Próxima implementação recomendada: autenticação e autorização mínima no backend.** Explicar a abordagem antes de escolher detalhes técnicos; não há decisão registrada de usar JWT, sessão ou outro mecanismo específico.

Critérios esperados: identificar o usuário conectado, impedir ações em nome de terceiros, restringir administração e vinculação de perfis, proteger recursos conforme seus participantes. Testar acesso sem autenticação, login válido/inválido, acesso próprio, tentativa de acesso/alteração de recurso alheio e ação administrativa sem permissão.

Esta etapa documental não implementou essa funcionalidade. Registrar em futuras entregas o que mudou, quais verificações foram realmente executadas, limitações e situação de arquivos locais, commits e GitHub.
