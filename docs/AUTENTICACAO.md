# Autenticação e permissões do backend

Implementação local de 25/09/2026. O frontend ainda precisa integrar este contrato.

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

Ainda faltam telas de cadastro/login e integração da publicação no frontend. Recuperação de senha, verificação de e-mail e limitação de tentativas de login não foram implementadas. Para implantação com HTTPS será necessário configurar cookie Secure e as origens reais. Problemas de estoque e concorrência identificados na auditoria permanecem separados desta etapa.

O usuário autorizou o commit e o push desta etapa na branch `feat/home-grafite-assistente`. A confirmação de publicação deve ser feita pelo histórico Git e pela referência remota. O envio dos arquivos não inclui os dados do MariaDB nem constitui backup do banco.
