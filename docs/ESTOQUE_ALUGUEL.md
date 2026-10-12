# Estoque de aluguel no MVP

## Regra implementada

Cada kit continua aceitando apenas um aluguel ativo por vez, preservando o fluxo existente. O item pode pedir várias unidades desse kit, desde que existam ferramentas para todas elas. Um kit com duas unidades de uma ferramenta exige seis unidades ao reservar quantidade três.

Aluguéis RESERVADO, RETIRADO e ATRASADO comprometem ferramentas. Reservas futuras também comprometem imediatamente o estoque; não há calendário de disponibilidade nem reaproveitamento por intervalos. CANCELADO e DEVOLVIDO deixam de participar do cálculo. Remover um item de aluguel RESERVADO também deixa de comprometer seus componentes.

O cálculo considera todos os kits que usam uma ferramenta. A capacidade de referência é o menor valor entre quantidade_total e quantidade_disponivel cadastrados; dela se subtrai a soma dos componentes comprometidos em aluguéis ativos. O resultado nunca fica negativo. Esses campos não são decrementados/incrementados pelo fluxo de aluguel: a ocupação é derivada dos itens, evitando dupla devolução e ajustes automáticos em registros antigos. As consultas brutas de ferramentas ainda expõem os valores cadastrados; não devem ser usadas como saldo livre para reservar. O endpoint /kits/disponiveis filtra pelo cálculo efetivo, e a escrita sempre revalida.

Kit vazio, kit inativo/indisponível ou componente fora do status DISPONIVEL não pode ser reservado. A composição fica bloqueada enquanto houver aluguel ativo, inclusive quando o status do kit estiver incorreto. Cancelar/devolver não reativa kit INATIVO nem libera um kit ainda referenciado por outro aluguel ativo.

## Transações

Adicionar/remover itens e retirar/devolver/cancelar bloqueiam e recarregam o aluguel antes de verificar o estado. A reserva bloqueia o kit e suas ferramentas, estas em ordem crescente de ID. Alterar a composição também bloqueia o kit. As operações usam READ_COMMITTED para consultar compromissos confirmados depois da espera pelos bloqueios, evitando uma fotografia antiga do estoque.

Essas garantias pressupõem que as escritas passam pelos serviços. Alterações manuais em SQL não seguem esses bloqueios. A leitura pública é informativa; entre consultar o catálogo e reservar, outro usuário pode consumir o estoque.

## Dados anteriores e limites

Não foi criada migração, corrigido status histórico ou atualizado contador no banco. Antes da integração final, conferir os aluguéis ativos e seus componentes reais: cadastros anteriores podem estar vazios, exceder estoque ou ter quantidade_disponivel ajustada manualmente. O cálculo é conservador e pode bloquear reservas nessas situações; não presume que os dados antigos estejam corretos.

O teste de concorrência em MariaDB permanece necessário. Testes com repositórios simulados validam regras, mas não comprovam isolamento ou bloqueios. Entrega, taxa, interface administrativa e telas de reserva não fazem parte desta alteração.

## Verificação em 10/10/2026

Maven test passou com 55 testes: 15 de estoque e 38 de segurança usando mocks, mais 2 testes de contexto/consultas com MariaDB real. O teste de contexto mantém ddl-auto=validate; o teste acrescentado executa as duas consultas de estoque sem escrita. Isso confirma a sintaxe e execução, não o comportamento sob concorrência. Consulta direta ao banco tech_help confirmou catálogo e aluguéis vazios neste momento; nenhum registro foi inserido ou alterado. O log registrou aviso de que MariaDB 10.4.32 está abaixo do mínimo 10.6 suportado pelo Hibernate atual, embora as verificações tenham passado.

## Complemento: reserva transacional e concorrência

Na etapa de reservas integrada à interface, foi acrescentado POST /alugueis/reservar para criar aluguel e item juntos. ReservaIntegracaoTests executou em MariaDB real, com dados temporários próprios: uma solicitação acima do estoque não deixou aluguel persistido; duas reservas concorrentes de kits distintos compartilhando uma única ferramenta produziram uma aceitação e uma rejeição; valor e cancelamento também foram conferidos. Fixtures removidas e ausência dos registros temporários confirmada. Esta verificação supera a pendência anterior para esse cenário específico, sem comprovar todos os cenários concorrentes nem constituir teste de carga.

O teste é desabilitado por padrão. Sua execução explícita exige techhelp.rental.integration=true e techhelp.integration.database igual ao banco efetivo. Nunca direcionar ou limpar dados sem conferir o ambiente.
