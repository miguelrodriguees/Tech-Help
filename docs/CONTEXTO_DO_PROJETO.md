# Contexto e continuidade do Tech Help

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
