# Fechamento do MVP do Tech Help

## Escopo acordado em 11/10/2026

O usuário autorizou avançar pelas pendências sem confirmar cada etapa e aprovou aluguel com retirada/devolução no local e pagamento fora do site. Entrega com cálculo de taxa e pagamento online ficam para uma versão futura. Ajustes estéticos da Home, cabeçalho e símbolo continuam adiados. Não interpretar essa autorização como permissão para publicar, fazer push ou alterar bancos de forma destrutiva.

## Percursos que a primeira versão deve cobrir

1. Cliente e técnico criam suas contas e entram; cadastro público não concede ADMIN.
2. Cliente publica solicitação e acompanha suas propostas/pedidos.
3. Técnico consulta oportunidades e envia proposta; cliente escolhe uma.
4. Apenas um aceite vence por pedido, mesmo com requisições simultâneas.
5. Participantes acompanham atendimento; técnico agenda, inicia e conclui. Cancelamento limitado ao atendimento ainda não iniciado.
6. Cliente avalia atendimento concluído sem duplicar a avaliação.
7. Administrador cadastra/edita ferramentas, monta kits e gerencia manutenção/inativação.
8. Usuário reserva kit disponível, consulta suas reservas e cancela antes da retirada.
9. Administrador registra retirada/devolução; estoque comprometido impede reservas e edições incompatíveis.

Esses percursos estão implementados. Consultar CONTEXTO_DO_PROJETO.md para distinguir testes automatizados atuais de conferências anteriores no navegador. Não considerar cada linha como nova aprovação visual ou teste de carga.

## Conferência final no computador

Checkpoint proposto documentado em CHECKPOINT_MVP.md. Novo teste completo HTTP FluxoMvpIntegracaoTests executado separadamente em 11/10/2026 e aprovado; instruções e limites naquele documento. Para incluí-lo na suíte completa opt-in, acrescentar `-Dtechhelp.mvp.integration=true` ao comando abaixo. A conferência visual atual preserva o atendimento existente do usuário.

Verificação de integridade em 11/10/2026: suíte completa de 72 testes do backend passou com todas as integrações opt-in habilitadas; 36 testes do frontend, build e lint passaram. As integrações criam registros próprios e removem somente esses registros. No CMD, após conferir o banco configurado:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help\backend"
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd -B -ntp "-Dtechhelp.review.integration=true" "-Dtechhelp.proposal.integration=true" "-Dtechhelp.rental.integration=true" "-Dtechhelp.integration.database=tech_help" "-Dspring.jpa.show-sql=false" test
```

- Reiniciar o backend no NetBeans para carregar as últimas correções. O teste Maven usa outro processo.
- Abrir o frontend existente. Se necessário, no CMD: `cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help\frontend"` e `npm.cmd run dev`.
- Conferir os percursos por perfil e o uso em tela estreita, especialmente formulários e mensagens de erro. Inspeção com viewport estreito não equivale a teste em celular físico.
- Manter catálogo TESTE e aluguéis históricos identificados. Não limpar tabelas para preparar uma apresentação.
- Criar um checkpoint Git revisado quando autorizado. Arquivos locais e dados do MariaDB são coisas diferentes: commit não faz backup do banco.

## Limites e trabalho posterior

- Recuperação de senha e verificação de e-mail ainda não existem; não anunciar essas funções. Devem ser planejadas antes de abrir o serviço a público amplo.
- Publicação na internet ainda exige destino de hospedagem, HTTPS, configuração de origem/cookies, credenciais de banco próprias e backup/restauração conferidos. A versão local não comprova prontidão para operação pública.
- MariaDB 10.4.32 gera aviso de versão abaixo da suportada pelo Hibernate atual. Não foi atualizado ou recriado; eventual atualização precisa de backup verificado.
- Estoque atual compromete o kit até cancelar/devolver, sem agenda por intervalos. Manutenção afeta toda a ferramenta, não unidades individuais.
- Retirada antecipada é permitida pela regra atual; não há multa automática nem confirmação bancária de pagamento. O módulo interno de pagamento simulado não representa cobrança real.
- Perfil profissional completo, chat, anexos e conteúdo adicional de categorias não são critérios adicionais de encerramento deste fluxo básico; não apresentá-los como concluídos quando só existe estrutura.

Próximo bloco depois das correções de integridade: conferência final dos percursos e preparação de um checkpoint local revisável. Modelo recomendado: **Sol — raciocínio médio** para documentação e verificações usuais; **Astra — raciocínio médio** se surgir falha de concorrência ou segurança.
