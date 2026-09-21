# Cadastro e login — etapa local em desenvolvimento

O frontend usa o cadastro existente de cliente, seguido de login com e-mail e senha. O Spring Security autentica contra os hashes BCrypt existentes. Não há novas tabelas, JWT ou armazenamento de senha/token de sessão no JavaScript.

A sessão fica em cookie HttpOnly. O frontend consulta GET /auth/csrf antes de cada operação de conta e envia o token no cabeçalho informado. GET /auth/me retorna somente identificação da conta e do cliente. POST /auth/logout encerra a sessão. O login utiliza POST /auth/login com formulário email/senha, tratado pelo filtro do Spring Security.

O diálogo de acesso não desmonta o assistente: ao entrar, ele fecha e mantém a mesma etapa e respostas. Fechar ou falhar no login também preserva o pedido. O cadastro profissional ainda não possui formulário. A publicação continua pendente: a conta conectada não significa que um pedido foi enviado.

## Mudança de acesso à API

Públicos: GET /categorias, GET /auth/csrf e POST /cadastro/cliente, /cadastro/tecnico, /auth/login. Cadastro e login exigem CSRF válido. /auth/me exige autenticação. Logout é tratado pelo filtro de segurança.

Os demais endpoints foram bloqueados por padrão até receberem autorização por proprietário. Isto altera os testes manuais antigos: endpoints antes abertos passarão a retornar 401/403. Não liberar genericamente todos os endpoints para qualquer usuário autenticado. O fluxo seguinte deve associar a solicitação ao cliente da sessão, sem confiar em idCliente enviado pelo navegador.

## Verificação

Frontend: build e lint passaram. Backend: compilação bloqueada por falha de resolução de repo.maven.apache.org. O ambiente local de trabalho também dispõe de Java 17, e o projeto requer Java 25. Cadastro/login com banco não foram testados de ponta a ponta.

No ambiente Windows com Java 25, iniciar MariaDB, executar mvnw.cmd test na pasta backend e iniciar o backend. Na pasta frontend, executar npm run dev e abrir http://localhost:5173 (origem permitida pelo CORS).

Conferir: cadastro novo; e-mail duplicado; senha inválida; conta inativa; login válido; recarregar e manter sessão; sair; voltar ao pedido sem perder respostas; tentativa de POST sem CSRF recusada; endpoints ainda não autorizados recusados. Usar apenas dados de teste. Antes de publicação externa, configurar HTTPS/cookie Secure e limitar tentativas de login.

Referências: https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html e https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html

## Etapa seguinte: publicação e consulta do cliente

Implementados POST /solicitacoes e GET /solicitacoes/minhas para usuários autenticados com conta de cliente ativa. O DTO de criação não recebe mais idCliente: ele é resolvido no servidor pela identidade da sessão. Endereço informado continua sendo validado contra esse cliente. Os endpoints antigos de consulta por ID/cliente e a lista pública de abertas continuam bloqueados até a etapa do técnico.

O frontend traduz as respostas do assistente para título, descrição, categoria, atendimento e urgência. Cidade/bairro integram a descrição presencial; não representam cadastro de endereço. Urgência de hoje é ALTA, próximos dias NORMAL e sem urgência BAIXA. A descrição contém somente a categoria atual e as perguntas visíveis. Não há promessa de atendimento no mesmo dia.

Publicação exige revisão e confirmação explícita após login. Erros preservam o rascunho. Sucesso mostra o ID retornado e limpa o rascunho. Minhas solicitações consulta dados reais da API e apresenta carregamento, vazio, erro e sessão expirada.

Reenvios usam Idempotency-Key e reaproveitam o resultado na mesma sessão. Essa proteção é local à sessão, não é persistente: após reinício do servidor, troca de sessão ou recarga da página que gere outra chave, consultar Minhas solicitações antes de reenviar uma resposta que ficou incerta. Persistência de idempotência pode ser acrescentada posteriormente se necessário.

Frontend: build, lint e nove testes de regras/conversão passam. Foram adicionados testes de identidade do cliente no backend, ainda não executados pelas limitações de Java/dependências já descritas. Fluxo com banco continua NÃO TESTADO.

## Etapa: técnico e propostas

Implementação local: cadastro de técnico pelo diálogo (anos de experiência e apresentação), identificação de perfil em `/auth/me`, área profissional com solicitações ABERTA/EM_NEGOCIACAO e propostas próprias. POST `/propostas` usa o técnico da sessão; o navegador não envia `idTecnico`. Os caminhos antigos que aceitam IDs para listar propostas continuam bloqueados.

A criação da proposta e a passagem do pedido para EM_NEGOCIACAO são uma transação. Bloqueio pessimista na solicitação serializa envios concorrentes; a regra existente de uma proposta por técnico/pedido permanece. Pedidos próprios e encerrados são recusados. Perfil PENDENTE mantém a permissão existente de propor; não se afirma que houve credenciamento. Inclusão de perfil técnico numa conta cliente existente não foi implementada.

Verificações realizadas: build TypeScript/Vite, ESLint e nove testes frontend existentes passaram. Esses testes cobrem assistente e payload de solicitação, não validam o novo fluxo de propostas. Foram escritos quatro testes unitários de PropostaService; NÃO EXECUTADOS. Backend e integração MariaDB permanecem NÃO TESTADOS neste ambiente (Java 17 disponível, projeto Java 25 e resolução Maven indisponível).

Roteiro de validação local: com Java 25, executar `mvnw.cmd test` no backend, iniciar banco/backend e `npm run dev` no frontend. Criar uma conta de cliente e publicar um pedido. Sair, cadastrar um técnico, abrir Área profissional e enviar proposta. Atualizar e confirmar sua presença em Minhas propostas; confirmar que o mesmo pedido não permite segunda proposta. Conta cliente não deve acessar os endpoints de técnico. Validar rejeição de pedido encerrado e envio concorrente no banco. A próxima etapa é a visualização/aceitação pelo cliente e criação do atendimento; ainda não foi conectada nesta entrega.

## Etapa: cliente compara e aceita propostas

Em Minhas solicitações, o cliente abre propostas e atendimento. A consulta retorna nome real do profissional, mensagem, valor, prazo e disponibilidade. Confirmação explícita antes de contratar; não há pagamento. Consulta exige propriedade do pedido. Aceitação exige cliente da sessão, usa a trava da solicitação (a mesma do envio) e transação: aceita uma proposta, recusa outras ENVIADAS, contrata pedido e cria serviço. Repetir aceitação da mesma proposta retorna o serviço existente. Rotas: GET `/cliente/solicitacoes/{id}/propostas`, GET `/servicos/meus`, POST `/servicos/aceitar-proposta/{id}`.

Mantido status legado AGENDADO no banco; como não há horário acertado, a interface mostra “Aguardando atendimento” e data a combinar. A data sugerida na proposta não é um agendamento confirmado. Comandos antigos de iniciar/concluir continuam bloqueados até implementação de autorização por participante; conclusão e avaliação são etapas seguintes.

Build, lint e nove testes frontend existentes passaram após esta etapa. Quatro testes de AceitarPropostaTest escritos (dono, contratação/recusa, repetição, pedido contratado), ainda NÃO EXECUTADOS pelas limitações Java/Maven já descritas. Concorrência, transações e integração real precisam de execução com MariaDB. Roteiro: publicar como cliente, propor com dois técnicos, entrar novamente como dono, confirmar uma proposta, conferir serviço e recusa da outra, repetir a chamada e verificar ausência de duplicação. Outro cliente deve receber 404 ao consultar/aceitar pedido alheio.

## Etapa: início, conclusão e avaliação

Implementação local: Meus atendimentos na área técnica; técnico contratado confirma início, cliente dono confirma conclusão. ServiceCard compartilhado mostra status, datas e avaliação. Nota 1–5 e comentário até 3000 caracteres. Autor/destinatário são derivados do serviço e da sessão; nenhum ID de usuário vem do formulário. Mantidas regras de serviço concluído e uma avaliação por par de participantes. Leitura nesta interface exibe somente avaliação escrita pelo cliente do serviço, inclusive para o técnico.

AtendimentoService verifica propriedade antes de agir, trava o serviço durante cada mutação e executa em transação. Repetições de início/conclusão no mesmo estado retornam o registro sem alterar datas. Rotas antigas desprotegidas continuam negadas no SecurityConfig; novas rotas `/atendimentos` exigem sessão e resolvem perfil. A conclusão atual é confirmação direta do cliente após início; não foi criado estado de solicitação de conclusão pelo técnico, nem alterações de tabelas.

Verificação: build, lint e nove testes frontend existentes passaram. Seis testes AtendimentoServiceTest adicionados, NÃO EXECUTADOS. Isso não comprova integração de início/conclusão/avaliação. Backend ainda exige Java 25 e dependências Maven indisponíveis neste ambiente. No computador de Miguel Java 25.0.4 foi confirmado pelo terminal.

Roteiro pendente: técnico contratado inicia; outro técnico recebe 404. Cliente correto confirma; outro cliente recebe 404. Serviço e solicitação ficam concluídos na mesma transação. Cliente avalia, recarrega, e técnico vê nota/comentário reais. Repetir avaliação deve falhar sem duplicar registro. Tentar concluir antes de iniciar e avaliar antes de concluir deve ser recusado. Testar envio simultâneo e perda de resposta. Agendamento de horário, cancelamento e avaliação do cliente pelo técnico não estão conectados nesta etapa.

## Revisão de cadastro/login após entrega da branch

Correções pontuais: DTOs de cadastro validam no servidor nome (120), e-mail (254), telefone (20) e apresentação profissional (3000), alinhados à tela/banco. Cadastro injeta o PasswordEncoder configurado no SecurityConfig, usa Locale.ROOT ao normalizar e-mail e recusa senha acima de 72 bytes UTF-8 antes de chamar BCrypt. A tela bloqueia submissões simultâneas com ref além do estado de carregamento.

Testes: build/lint e nove testes frontend existentes passaram, sem cobertura específica de autenticação. Dois testes CadastroServiceTest adicionados para limite de senha multibyte e duplicidade de e-mail; NÃO EXECUTADOS aqui por limitações Java/Maven. Persistem pendências: teste real sessão/CSRF/CORS, recuperação de senha, verificação de e-mail e tratamento amigável de cadastros concorrentes que conflitem nas chaves únicas. CPF atualmente valida formato de 11 dígitos, não dígitos verificadores. Não considerar autenticação homologada para produção.
