# Orientações para trabalhar no Tech Help

## Produto e escopo

- Marketplace de serviços de TI com aluguel de ferramentas pertencentes ao próprio Tech Help.
- Manter o MVP simples: sem IA no produto, recomendação sofisticada ou abstrações sem utilidade clara.
- Preservar a estrutura existente: backend Java/Spring Boot, frontend React/TypeScript e MariaDB. Fazer mudanças incrementais, sem recriar o projeto ou substituir tecnologias por preferência.
- Consultar [o contexto do projeto](docs/CONTEXTO_DO_PROJETO.md) ao retomar o desenvolvimento, decidir escopo, trabalhar com banco ou investigar pendências da auditoria. As observações datadas precisam ser reconfirmadas quando relevantes.

## Comunicação e implementação

- Responder em português, com explicações claras para quem está aprendendo Java, SQL, Spring, REST, JPA, Maven, React e TypeScript.
- Antes de mudanças importantes, explicar o problema, os arquivos envolvidos e a razão da solução; depois implementar, verificar e relatar o resultado. Não explicar cada linha trivial.
- Distinguir planejado, implementado e testado; separar falhas reproduzidas de riscos identificados por leitura. Não apresentar testes antigos como uma nova execução.
- Não apagar código ou recursos aparentemente sem uso antes de entender sua finalidade e referências.
- Adicionar testes de comportamento quando pertinentes à mudança, especialmente para regras de negócio e permissões. Não exigir build completo para alterações exclusivamente documentais.

## Git e continuidade

- Conferir branch e alterações locais antes de editar; preservar trabalho existente do usuário.
- O frontend mais recente da auditoria de 23/09/2026 estava em `feat/home-grafite-assistente`, commit `a814eb8`, dois commits à frente de `main`. Verificar o estado atual; não fixar o desenvolvimento permanentemente nesse commit.
- Não trocar de branch, fazer merge, commit ou push implicitamente para organizar o projeto. Respeitar o escopo autorizado na tarefa.
- Informar separadamente o que está salvo localmente, registrado em commit e confirmado no GitHub. Uma referência remota local pode estar desatualizada.

## Banco e ambiente

- `tech_help` e `tech_help_teste` são bancos existentes; o de testes também contém dados anteriores. Não apagar, recriar, sobrescrever ou importar o script inicial sobre eles sem necessidade e autorização específica para a operação.
- Preservar `ddl-auto=validate`. Não mudar para `create`, `create-drop` ou `update` para contornar falhas de inicialização.
- Antes de testes com escrita, conferir o banco efetivamente usado. Isolar e identificar os próprios registros temporários; não limpar dados preexistentes.
- Não confundir `database/techhelp_v2_mariadb_limpo.sql` com backup dos registros atuais. O backup mencionado pelo usuário ainda não teve localização ou integridade verificadas.
- Em falha de conexão, verificar primeiro o MariaDB e a configuração. Não presumir que um erro de Dialect exige trocar o Dialect.
- Não presumir acesso ao XAMPP local em ambiente remoto nem persistência dos processos entre tarefas.

## Comandos usuais de verificação

No PowerShell, dentro de `backend/`:

```powershell
.\mvnw.cmd -B -ntp compile
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp spring-boot:run
```

O teste de contexto existente depende do MariaDB e, sem sobrescrita, utiliza `tech_help` pela configuração padrão. Inspecionar os testes antes de executar uma suíte modificada. Em sistemas compatíveis, usar `./mvnw`.

Dentro de `frontend/`:

```powershell
npm.cmd test
npm.cmd run build
npm.cmd run lint
```

Usar `npm ci` quando for necessário instalar as dependências do lockfile. No Windows, `npm.cmd` evita depender da política de execução de scripts PowerShell. Falhas de permissões do ambiente devem ser distinguidas de defeitos do projeto.
