# Aceitação concorrente de propostas

## Regra implementada

O aceite e o envio de propostas bloqueiam a mesma solicitação com PESSIMISTIC_WRITE, dentro de transações READ_COMMITTED. O refresh relê o estado persistido, inclusive quando a autorização ou outro acesso já carregou a entidade no contexto JPA. O aceite bloqueia primeiro a solicitação e depois a proposta, antes das validações e gravações.

Assim, duas propostas do mesmo pedido não são aceitas simultaneamente. Um segundo aceite é recusado pela regra existente; não gera outro serviço. Se um envio terminar antes do aceite, sua proposta é recusada junto das demais; se chegar depois, é rejeitado porque a solicitação já está contratada. A transação mantém aceite, recusa das demais, contratação e criação do serviço juntos. Não houve mudança de tabelas.

## Verificação em 11/10/2026

Compilação e 49 testes passaram: 46 em SegurancaApiTests e 3 em PropostaConcorrenciaTests. Os três cenários reais no MariaDB são: propostas distintas do mesmo pedido, aceite repetido da mesma proposta e envio concorrente ao aceite. Duas transações carregam entidades antes de uma barreira comum; depois executam as operações e verificam o estado final. Conferidos serviço único, proposta aceita única, ausência de propostas ENVIADA e correspondência de técnico/valor.

Banco efetivo confirmado como tech_help antes das escritas. Fixtures TESTE-PROPOSTA com UUID, usuários próprios sem senha utilizável, cliente, técnicos, categoria e pedido próprios. Limpeza restrita aos IDs criados, após encerrar as threads. Consulta final confirmou zero usuários/categorias temporários e contagens originais: 3 usuários, 1 solicitação, 1 proposta e 1 serviço. Nenhum dado anterior foi usado como fixture.

Isso não é teste de carga nem cobertura de todas as transições concorrentes. A brecha anterior foi identificada por leitura; não foi executada uma versão propositalmente desprotegida. O teste usa serviços reais com autorização Spring e transações reais, sem navegador; permissões HTTP continuam cobertas pela suíte de segurança com mocks. Avaliação duplicada e concorrência da edição do catálogo permanecem pendentes.

## Repetir pelo CMD

Com o MariaDB ativo, conferir primeiro o endereço e o banco em application.properties. O teste é opt-in e compara SELECT DATABASE() com o nome explicitamente informado antes de criar registros. Não alterar ddl-auto=validate. O teste altera temporariamente o banco indicado e remove somente seus próprios registros.

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help\backend"
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd -B -ntp "-Dtest=SegurancaApiTests,PropostaConcorrenciaTests" "-Dtechhelp.proposal.integration=true" "-Dtechhelp.integration.database=tech_help" "-Dspring.jpa.show-sql=false" test
```

O aviso do Hibernate sobre MariaDB 10.4.32 abaixo da versão suportada (10.6) permaneceu; não impediu os testes. Não foi trocado o Dialect nem atualizado o banco nesta etapa.

Reiniciar o backend em execução no NetBeans para carregar a correção no site. A execução dos testes não substitui aquele processo.
