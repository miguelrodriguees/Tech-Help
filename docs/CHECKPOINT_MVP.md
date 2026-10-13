# Checkpoint preparado do MVP

## Estado do Git

Durante a preparação em 11/10/2026: branch feat/home-grafite-assistente, último commit local e45b296 (`feat: integra login, propostas e previa aprovada no frontend`). As mudanças posteriores estavam na pasta de trabalho, incluindo arquivos novos ainda não rastreados. Após revisar este checkpoint e conferir as três contas, o usuário autorizou explicitamente criar o commit local com o título abaixo. Conferir o hash resultante com `git log -1`; este documento faz parte do próprio checkpoint. Push não autorizado nem realizado nesta etapa; o estado atual no GitHub não foi consultado.

## Conteúdo do checkpoint proposto

- Backend: acompanhamento, agendamento/cancelamento do serviço, avaliação do cliente, identificação mínima dos técnicos, controle de estoque, reserva integrada, administração de aluguéis e edição/manutenção do catálogo.
- Integridade: bloqueios entre operações simultâneas de proposta, avaliação, reserva e manutenção; validação de tamanhos e precisão monetária; retorno das datas após gravação, compatível com MariaDB 10.4.
- Frontend: telas de serviços/avaliação, aluguel/reserva, administração de catálogo e aluguéis, tratamento de sessão/CSRF e confirmação antes das escritas. Mantidos os ajustes anteriores de navegação.
- Testes: segurança, regras de estoque, concorrência real no MariaDB, percurso completo HTTP e testes Node das operações do frontend.
- Documentação: contexto de continuidade, escopo do MVP e instruções/evidências das verificações.

Mensagem sugerida para um checkpoint único: `feat: conclui fluxos locais de serviços e aluguel do MVP`.

Antes de gravar, conferir `git status` e incluir explicitamente os fontes, testes e documentos novos. Os arquivos em docs/evidencias são capturas locais do sistema para comprovar testes e podem ser mantidos junto da documentação. Não incluir logs, target, dist, node_modules, arquivos .env, credenciais ou dumps do banco. Esses itens não são necessários para compilar os fontes.

## Evidência de testes

Na etapa anterior: compilação e 72 testes completos do backend passaram com integrações opt-in habilitadas; 36 testes Node, build e lint do frontend passaram. Nesta preparação: novo FluxoMvpIntegracaoTests executado separadamente e passou, com controllers, autenticação por formulário, sessão, autorização, JPA e MariaDB reais, sem repositories simulados.

O novo percurso verifica cadastro de cliente/técnico, login, bloqueio anônimo e sem CSRF, publicação, proposta, aceite exclusivo do cliente, recusa de conclusão antes do início, recusa de início pelo cliente, execução/conclusão pelo técnico, avaliação única e logout. Confere também datas preenchidas nas respostas de criação.

Fixtures TESTE-FLUXO com UUID e contas próprias, removidas ao final. Consulta posterior: 3 usuários, 1 solicitação, 1 proposta, 1 serviço, 0 avaliações, zero usuários/categorias temporários do teste. Não houve nova execução de toda a suíte de 73 testes nesta preparação: os 72 anteriores e o teste novo são execuções distintas.

Com MariaDB ativo e banco efetivo conferido, repetir apenas o novo teste no CMD:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help\backend"
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd -B -ntp "-Dtest=FluxoMvpIntegracaoTests" "-Dtechhelp.mvp.integration=true" "-Dtechhelp.integration.database=tech_help" "-Dspring.jpa.show-sql=false" test
```

## Limites do checkpoint

Conferência visual desta etapa: cliente carregou pedido/proposta/atendimento e seus aluguéis; técnico carregou proposta aceita e ações do atendimento; ADMIN carregou catálogo, composição e histórico de aluguéis. Nenhuma falha nova observada nessas consultas. Evidências em docs/evidencias/cliente-*, tecnico-* e admin-*-conferencia-final.jpg. Os registros existentes permaneceram intactos.

O banco e seus registros não ficam dentro do commit. Seu backup precisa ser preservado separadamente. Não há migração nova ou alteração de ddl-auto.

A conferência visual de contas existentes é em leitura: não conclui nem cancela o atendimento do usuário só para demonstrar um percurso. O percurso de escrita foi validado pela API com fixtures próprias; isso não equivale a testar cada botão de criação em navegador.

O escopo aprovado é funcionamento local com retirada/devolução no Tech Help e pagamento fora do site. Hospedagem pública, recuperação de senha, verificação de e-mail, entrega/taxa, pagamento online e ajustes estéticos seguem conforme FECHAMENTO_MVP.md. Não declarar a operação pública pronta com base nesses testes locais.
