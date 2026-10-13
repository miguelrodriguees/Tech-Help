# Demonstração do MVP do Tech Help

## Preparação

Iniciar MariaDB no XAMPP, executar o backend no NetBeans e iniciar o frontend. Pelo CMD:

```bat
cd /d "C:\Users\Miguel Rodrigues\Desktop\Tech-Help\frontend"
npm.cmd run dev
```

Abrir o endereço informado pelo Vite, normalmente http://localhost:5173. Usar as contas já existentes e digitar as senhas pessoalmente. O MVP inclui serviços, avaliações e aluguel com retirada/devolução local e pagamento combinado fora do site.

## Percurso rápido, preservando os registros existentes

1. Mostrar a Home e a descrição guiada do problema. Explicar que a conta é necessária para publicar o pedido.
2. Entrar como cliente. Abrir Meus pedidos e Ver propostas: pedido 1 contratado e proposta aceita de R$ 200,00.
3. Abrir Meus serviços: atendimento 1 aguardando início, identificação do técnico e data ainda não combinada. Não iniciar ou cancelar esse atendimento apenas para mostrar a tela.
4. Abrir Meus aluguéis: catálogo com kit TESTE a R$ 5,00/dia e histórico próprio dos aluguéis 4/5 cancelados. Mostrar que há revisão antes da confirmação e que o pagamento ocorre fora do site.
5. Sair e entrar como técnico. Abrir Área do técnico: proposta aceita e pedido contratado fora das oportunidades abertas. Em Meus serviços, mostrar ações de agendar, iniciar e cancelar conforme o estado.
6. Sair e entrar como ADMIN. Abrir Administrar catálogo: ferramenta com 5 unidades, kit e composição de uma unidade; mostrar campos de edição e manutenção sem gravar mudanças.
7. Abrir Administrar aluguéis: registros cancelados e aluguel 6 devolvido, com datas efetivas e valores preservados.
8. Explicar o limite aprovado: entrega/taxa, pagamento online, perfil profissional completo e publicação pública ficam para etapas posteriores. Ajustes estéticos estão adiados.

## Demonstração de escrita completa

Se for necessário demonstrar todas as ações ao vivo, criar um pedido novo identificado como DEMONSTRAÇÃO com descrição fictícia. Técnico envia proposta; cliente revisa e aceita; técnico agenda se necessário, inicia e conclui; cliente envia nota/comentário. Usar apenas esse novo atendimento para as ações. As transições devem ser realizadas na ordem, e a avaliação ocorre após conclusão.

Para aluguel, usar kit identificado de demonstração, datas futuras no horário de Brasília e quantidade disponível. Cliente revisa/confirma, ADMIN registra retirada/devolução apenas como simulação de demonstração, sem cobrança real. Manter o histórico identificado; não apagar tabelas nem redefinir o estoque manualmente.

## O que foi validado

Conferência recente das três contas existentes em leitura, sem alterar seu atendimento. Teste FluxoMvpIntegracaoTests passou separadamente com cadastro, login, publicação, proposta, aceite, execução, avaliação e logout pela API real e MariaDB, usando fixtures próprias removidas ao final. Testes anteriores cobrem regras, permissões, estoque e operações simultâneas; detalhes em CHECKPOINT_MVP.md e CONTEXTO_DO_PROJETO.md.

Não apresentar a conferência em leitura como execução de todos os botões pelo navegador. Teste em largura de celular também não equivale a teste em aparelho físico.
