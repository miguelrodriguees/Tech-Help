# Aluguel de ferramentas e kits — primeira versão para validação

## Regras deste módulo

Os equipamentos pertencem ao TechHelp. Clientes e técnicos autenticados podem alugar. O perfil ADMIN é atribuído pelo responsável pelo banco, nunca no cadastro público.

- Catálogo público com preços e disponibilidade reais do banco; vazio quando não há equipamentos cadastrados.
- Ferramentas avulsas e kits. Um kit consome as unidades de suas ferramentas; não possui um estoque independente.
- Retirada no TechHelp ou entrega no endereço; devolução no TechHelp ou coleta paga. Entrega e coleta usam o mesmo endereço.
- Transporte cotado manualmente pelo administrador conforme endereço/distância. Não há cálculo automático de quilômetros nem integração com mapas.
- Sem transporte: a confirmação do formulário reserva o estoque imediatamente.
- Com transporte: pedido → aguardando taxa → orçamento administrativo → aceite do usuário → reserva. Antes do aceite, não há reserva nem garantia de disponibilidade.
- Aluguel, entrega e coleta aparecem separados. O total fica indefinido enquanto faltar taxa. Não há pagamento online.
- Cancelamento antes da retirada/entrega libera estoque; após entrega, administrador registra a devolução após conferir o material.
- Diárias de 24 horas, frações arredondadas para cima; período máximo de 30 dias. Estas são regras iniciais, ajustáveis antes do lançamento.
- Estoque fica comprometido desde a confirmação até cancelamento/devolução: esta versão não faz reservas de capacidade por calendário.
- Horários e logística precisam ser combinados com o TechHelp. A interface não promete disponibilidade de entrega em um horário específico.

## Atualização segura do banco de testes (Windows)

O banco `tech_help_teste` já existe. NÃO execute novamente o schema inicial e NÃO apague o banco.

1. Pare o backend com Ctrl+C.
2. Na pasta Tech-Help-validacao, execute `git pull --ff-only`.
3. Execute `node scripts/preparar-migracao-aluguel.mjs`.
4. No DBeaver, abra `backend/target/migracao-aluguel-teste.sql` e associe à conexão MariaDB local.
5. Configure execução para PARAR no primeiro erro. O script contém `USE tech_help_teste`. Execute uma única vez como script SQL.
6. Se falhar, pare e guarde a mensagem. MariaDB confirma DDL por instrução; não execute o arquivo inteiro novamente sem verificar quais alterações foram aplicadas.

A migração acrescenta campos de transporte, referência de ferramenta avulsa, estados de cotação e a tabela `aluguel_consumo` (quantidades reservadas por pedido). Preserva os demais dados. Ela bloqueia a aplicação se existirem aluguéis antigos RESERVADO/RETIRADO/ATRASADO: nesses casos é necessário conciliar estoque primeiro, sem apagar registros.

Confira em nova consulta:

```sql
SHOW COLUMNS FROM tech_help_teste.aluguel;
SHOW TABLES FROM tech_help_teste LIKE 'aluguel_consumo';
```

Inicie o backend no mesmo CMD:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help-validacao\backend"
set "SPRING_DATASOURCE_URL=jdbc:mariadb://127.0.0.1:3306/tech_help_teste"
mvnw.cmd test
mvnw.cmd spring-boot:run
```

Confira no log a URL com **tech_help_teste**. Deixe a janela aberta após `Started TechhelpBackendApplication`. Em outro CMD, inicie o frontend:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help-validacao\frontend"
npm run dev
```

Use http://localhost:5173. Não altere o banco original `tech_help` nesta validação.

## Atribuir ADMIN à sua conta de teste

Cadastre uma conta pelo site ou use sua conta já cadastrada no banco de testes. No DBeaver, substitua o e-mail abaixo e consulte primeiro:

```sql
SELECT id_usuario, nome, email FROM tech_help_teste.usuario
WHERE email = 'SEU_EMAIL_DE_TESTE';
SELECT id_perfil, nome FROM tech_help_teste.perfil WHERE nome = 'ADMIN';
```

Só prossiga se o usuário mostrado for a conta que deve administrar o TechHelp e houver o perfil ADMIN. Execute:

```sql
INSERT INTO tech_help_teste.usuario_perfil (id_usuario, id_perfil)
SELECT u.id_usuario, p.id_perfil
FROM tech_help_teste.usuario u CROSS JOIN tech_help_teste.perfil p
WHERE u.email = 'SEU_EMAIL_DE_TESTE' AND p.nome = 'ADMIN'
AND NOT EXISTS (
 SELECT 1 FROM tech_help_teste.usuario_perfil up
 WHERE up.id_usuario = u.id_usuario AND up.id_perfil = p.id_perfil
);
```

Saia e entre novamente no site. O link **Administrar aluguéis** deve aparecer. O backend verifica o perfil em cada operação administrativa; esconder o link não é a proteção de acesso.

## Teste integrado automatizado (apenas banco de testes)

Com backend conectado a `tech_help_teste`, configure temporariamente no CMD as credenciais dessa conta administrativa. Não envie sua senha ao chat e não salve credenciais no Git.

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help-validacao"
set "TECHHELP_ADMIN_EMAIL=SEU_EMAIL_DE_TESTE"
set "TECHHELP_ADMIN_SENHA=SUA_SENHA_DE_TESTE"
node scripts/validar-aluguel.mjs --banco-de-testes
set "TECHHELP_ADMIN_SENHA="
set "TECHHELP_ADMIN_EMAIL="
```

O script cria usuários e equipamento identificados como TESTE; não realiza entregas. Mantém os registros para inspeção e inativa a ferramenta ao final. Verifica restrições de acesso, cotação, aceite, reenvio do pedido, estoque compartilhado, concorrência entre reservas, cancelamento e devolução repetida. Se falhar, a execução pode deixar reservas de teste que devem ser consultadas antes de repetir. O argumento `--banco-de-testes` confirma sua intenção; não detecta automaticamente qual banco o servidor usa.

Depois, execute novamente `node scripts/validar-fluxo.mjs --banco-de-testes` para verificar que o fluxo de serviços foi preservado.

## Teste manual

1. ADMIN cadastra uma ferramenta e monta um kit com ela, usando dados de teste identificados.
2. Visitante escolhe item, período e transporte; ao enviar, faz login e confere que as escolhas continuam na tela.
3. Cliente envia pedido com entrega/coleta; total fica aguardando cálculo.
4. ADMIN informa taxas e condições. Cliente atualiza Meus aluguéis, confere valores e aceita.
5. ADMIN registra entrega e depois devolução. Estoque volta ao valor anterior.
6. Repita com retirada/devolução no local; confirme ausência de taxas de transporte.
7. Confira em celular ou janela estreita: formulários, resumo, menus e botões sem sobreposição.

## Implementação e limites

Endpoints novos em `/locacao`; rotas antigas de aluguel continuam bloqueadas. O usuário vem da sessão, não do corpo do pedido. CSRF continua ativo. `LocacaoService` usa JDBC no mesmo banco e transações Spring, com bloqueio ordenado das ferramentas durante reserva. `aluguel_consumo` registra a composição utilizada para que uma devolução reponha exatamente as quantidades reservadas.

Nesta versão, `aluguel.valor_total` guarda o subtotal do aluguel; o total apresentado pela nova API soma esse valor às taxas de transporte. Preços por item são congelados na criação do pedido. Registros legados não aparecem nas operações novas.

A administração inicial permite cadastrar ferramentas/preços/quantidades, montar kits, alterar situação de ferramentas e operar pedidos. Edição posterior de preços/composição, reposição de estoque, gestão individual por número de série, danos/multas/caução, cálculo automático de distância, agenda e pagamento ficam para etapas posteriores. Uma cotação enviada não pode ser alterada: cancele o pedido e refaça se necessário. Lista limitada aos últimos 100 pedidos; atraso não muda automaticamente de estado.

## Evidência de validação desta entrega

- Frontend: build e lint passaram; 9 testes existentes passaram. Eles validam o assistente de serviços, não o novo fluxo de aluguel.
- Gerador da migração executado; sintaxe do script Node de teste conferida.
- Incluídos 13 testes Java de regras e acesso/transições. Não executados neste ambiente: Java 25/JDK e dependências Maven indisponíveis; o Maven parou na resolução do parent Spring Boot.
- Migração MariaDB, testes integrados HTTP e revisão visual em navegador ainda precisam ser executados no ambiente local de validação. Não tratar esta entrega como pronta para produção antes desses resultados.
