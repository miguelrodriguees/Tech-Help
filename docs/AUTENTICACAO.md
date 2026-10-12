# Autenticação e permissões do backend

Backend implementado em 25/09/2026. O frontend agora integra cadastro, login, logout, consulta de sessão e publicação; consulte [o README do frontend](../frontend/README.md).

## Como funciona

O login usa o e-mail e a senha cadastrados. O Spring Security compara a senha com o hash BCrypt e mantém a identidade em uma sessão do servidor. O navegador guarda apenas o cookie `JSESSIONID`, marcado como HttpOnly e SameSite=Lax. A sessão expira após 30 minutos de inatividade e é invalidada no logout. Reiniciar o backend encerra as sessões em memória.

`SecurityConfig` habilita a segurança web e as permissões por método. Os services existentes usam `@PreAuthorize`: o padrão da classe exige ADMIN; as regras dos métodos liberam operações públicas ou verificam o dono/participante. `AcessoService` consulta a identidade, o status e os perfis atuais no banco, evitando confiar apenas nos IDs enviados pela interface. Bloquear a conta ou remover um perfil afeta as operações protegidas na sessão existente.

O cadastro continua público e atribui CLIENTE ou TECNICO de acordo com a rota. Não aceita escolher ADMIN. Nenhum administrador foi criado nesta etapa. O hash da senha não é serializado nas respostas JSON de usuário.

## Contrato HTTP

| Operação | Requisição | Resultado esperado |
| --- | --- | --- |
| Obter proteção CSRF | `GET /auth/csrf` | JSON com `token`, `headerName` e `parameterName`; cria/preserva a sessão |
| Entrar | `POST /auth/login`, formulário `application/x-www-form-urlencoded` com `email` e `senha` | 204; credenciais inválidas ou conta não ativa retornam 401 |
| Consultar sessão | `GET /auth/me` | IDs de usuário/cliente/técnico, nome, e-mail e perfis; sem sessão retorna 401 |
| Sair | `POST /auth/logout` | 204 e invalidação da sessão |

Login, logout, cadastro e demais POST/PUT/PATCH/DELETE exigem o token CSRF. Enviar o valor de `token` no cabeçalho indicado por `headerName`. Um token ausente/inválido retorna 403, inclusive antes do login.

Sequência para a interface:

1. Usar `withCredentials: true` no Axios para preservar o cookie em todas as chamadas.
2. Consultar `/auth/csrf` e guardar o token em memória.
3. Enviar o formulário de login com o cabeçalho CSRF.
4. Após login bem-sucedido, consultar novamente `/auth/csrf`: o token anterior é invalidado. Consultar `/auth/me` para obter a identidade real.
5. Usar o novo token nas alterações. Após logout, obter outro token antes de um novo cadastro/login.

Em desenvolvimento, o CORS permite credenciais somente de `http://localhost:5173`. Usar `localhost` também na URL da API; misturar `localhost` e `127.0.0.1` pode impedir o envio do cookie SameSite. Não guardar senha nem inventar IDs de usuário na interface. Os IDs do contrato existente continuam aceitos, mas são conferidos contra a sessão.

## Permissões aplicadas

| Área | Regra |
| --- | --- |
| Catálogo e perfil profissional | Leituras de categorias, especialidades, técnicos, portfólios, certificações, ferramentas, kits e avaliações por usuário são públicas |
| Solicitações | Cliente cria e lista as próprias; técnicos acessam oportunidades abertas/em negociação e solicitações nas quais já propuseram; ADMIN pode administrar |
| Propostas | Técnico cria/lista as próprias; o dono da solicitação lista as propostas recebidas e aceita uma delas |
| Serviços | Cliente e técnico participantes consultam; técnico responsável inicia/conclui; ADMIN pode administrar |
| Avaliações | Participante autenticado avalia em seu nome; as regras existentes exigem conclusão e a outra parte como avaliado |
| Dados pessoais | Usuário acessa seus dados, endereços, aluguéis e notificações; ADMIN tem acesso administrativo |
| Conversas | Criação liga o cliente da solicitação a um técnico proponente e exige que o chamador seja um dos dois; mensagens exigem participação e identidade própria; não podem imitar mensagens SISTEMA |
| Portfólio, certificações e especialidades | Escrita restrita ao técnico correspondente ou ADMIN |
| Administração | Vínculos de perfil, escrita no catálogo, confirmação de retirada/devolução, moderação e criação de notificações exigem ADMIN |
| Pagamentos | Consultas restritas aos participantes/dono ou ADMIN; operações de escrita do pagamento SIMULADO ficam administrativas, sem integração financeira real |

Recurso alheio ou inexistente pode retornar 403 antes da consulta de negócio, evitando revelar sua existência. Falta de login retorna 401 nas rotas privadas quando a requisição passa pela validação CSRF.

## Validação e limites

`SegurancaApiTests` usa filtros e services reais com repositories simulados. Testa autenticação, sessão/CSRF, permissões, cadastro, proteção do hash e o fluxo solicitação → proposta → aceite → início → conclusão → avaliação. Não grava no MariaDB e não comprova persistência, restrições SQL ou concorrência.

`TechhelpBackendApplicationTests` continua validando a inicialização com o banco configurado (por padrão `tech_help` e `ddl-auto=validate`). Não cria registros de teste.

As telas de cadastro/login e a publicação foram integradas em uma etapa posterior do frontend. Recuperação de senha, verificação de e-mail e limitação de tentativas de login não foram implementadas. Para implantação com HTTPS será necessário configurar cookie Secure e as origens reais. Problemas de estoque e concorrência identificados na auditoria permanecem separados desta etapa.

O usuário autorizou o commit e o push desta etapa na branch `feat/home-grafite-assistente`. A confirmação de publicação deve ser feita pelo histórico Git e pela referência remota. O envio dos arquivos não inclui os dados do MariaDB nem constitui backup do banco.

## Avaliação simplificada pelo cliente

`POST /avaliacoes/servico/{idServico}/cliente` recebe somente `nota` (inteiro de 1 a 5) e `comentario` (opcional, até 3000 caracteres). Exige sessão, CSRF e perfil CLIENTE do dono do atendimento; não permite ao técnico ou administrador avaliar em nome do cliente. O backend deriva os usuários avaliador/avaliado dos participantes registrados e reaproveita as regras de conclusão, proibição de autoavaliação e repetição. O endpoint anterior continua disponível com suas permissões existentes.

`GET /avaliacoes/servico/{idServico}` continua restrito aos participantes ou administrador e permite à interface consultar o resultado antes de oferecer um novo envio. Os testes com repositories simulados cobrem o novo endpoint e não comprovam concorrência nem as restrições do banco real.

## Identificação pública mínima dos técnicos

`GET /tecnicos/resumos?ids=20,21` usa a leitura pública já prevista para técnicos. Retorna exclusivamente `idTecnico` e `nome`, por DTO, sem serializar o usuário. Consulta de 1 a 50 IDs positivos; técnicos inexistentes, contas inativas e nomes ausentes não entram na resposta. Os endpoints anteriores de técnicos permanecem com o contrato existente. Os testes conferem a ausência de CPF, telefone, e-mail, hash e ID do usuário na nova resposta.

## Agendamento e cancelamento de serviços

- `POST /servicos/{idServico}/agendar`, corpo `{"dataAgendada":"2099-05-20T14:30:00"}`: técnico responsável ou ADMIN; serviço AGENDADO e data futura em America/Sao_Paulo.
- `POST /servicos/{idServico}/cancelar`: cliente/técnico participante ou ADMIN; somente AGENDADO. Atualiza serviço para CANCELADO e solicitação para CANCELADA na mesma transação. Não reabre pedido nem altera o histórico das propostas.

As quatro operações de edição do serviço (agendar, cancelar, iniciar, concluir) atualizam a entidade a partir do banco com `EntityManager.refresh` e bloqueio PESSIMISTIC_WRITE antes de conferir o estado, dentro da transação. Isso evita decidir a partir de uma entidade antiga carregada durante a autorização. Não houve alteração do esquema. O aceite de propostas não foi incluído nessa proteção. Testes com EntityManager/repositories simulados verificam regras e validação após atualização, mas não comprovam o bloqueio sob concorrência real no MariaDB.

## Catálogo administrativo integrado

O menu do frontend exige perfil ADMIN. A segurança definitiva permanece nos serviços: POST /ferramentas, POST /kits, POST /kits/{idKit}/ferramentas e DELETE /kits/{idKit}/ferramentas/{idFerramenta} exigem administrador e CSRF. Não foram abertas novas permissões nem criado cadastro público de administradores.

Testes MVC acrescentados verificam rejeição de clientes/técnicos, ausência de CSRF, cadastro por ADMIN, montagem/remoção e bloqueio da composição com aluguel ativo. Usam repositories simulados. A interface reconfirma o ID de usuário e o perfil administrativo antes de cada escrita; após falha, exige atualizar o catálogo e conferir o resultado.

## Reserva completa em uma transação

POST /alugueis/reservar recebe idUsuario, idKit, quantidade, dataPrevistaRetirada e dataPrevistaDevolucao. Exige dono da conta autenticado e CSRF. ReservaService chama os serviços existentes dentro de uma transação READ_COMMITTED; falha de estoque desfaz também o aluguel. O endpoint valida retirada futura em America/Sao_Paulo e reaproveita a exigência de devolução posterior. Não há permissão para reservar em nome de outro usuário, nem para o ADMIN nesse endpoint.

ReservaIntegracaoTests é opt-in (-Dtechhelp.rental.integration=true -Dtechhelp.integration.database=tech_help, indicando também a URL do datasource). Ele confere o banco efetivo antes de escrever; usa fixtures UUID próprias e limpeza somente por IDs gerados. A execução realizada comprovou rollback, disputa de ferramenta compartilhada e cancelamento. Testes MVC cobrem usuário errado, visitante, CSRF, quantidade e datas inválidas.
