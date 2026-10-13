import { test } from 'node:test';
import assert from 'node:assert/strict';
import { proposalPayload, openForProposals, money, dateLabel } from '../src/features/propostas/proposals.ts';

const fields = { valor: '150,50', mensagem: ' Diagnóstico incluído ', prazo: '2', data: '2026-10-01' };
test('proposta preserva valor decimal e usa técnico da sessão', () => {
  assert.deepEqual(proposalPayload(12, 34, fields), { idSolicitacao: 12, idTecnico: 34, valor: '150.50', mensagem: 'Diagnóstico incluído', prazoEstimadoDias: 2, dataDisponivel: '2026-10-01' });
  const minimal = proposalPayload(12, 34, { valor: '1', mensagem: '', prazo: '', data: '' });
  assert.equal(minimal.prazoEstimadoDias, null);
  assert.equal(minimal.dataDisponivel, null);
  assert.equal(minimal.mensagem, null);
});
test('valor, prazo e identidade inválidos não são enviados', () => {
  for (const valor of ['0','-10','abc','1.999','1e3','10000000000']) assert.throws(() => proposalPayload(1,2,{...fields,valor}));
  for (const prazo of ['-1','1.5','32768','x']) assert.throws(() => proposalPayload(1,2,{...fields,prazo}));
  assert.throws(() => proposalPayload(1,0,fields));
  assert.throws(() => proposalPayload(-1,2,fields));
});
test('apenas pedidos abertos ou em negociação permitem escolha', () => {
  assert(openForProposals('ABERTA')); assert(openForProposals('EM_NEGOCIACAO'));
  for (const status of ['CONTRATADA','CONCLUIDA','CANCELADA']) assert.equal(openForProposals(status),false);
  assert.match(money(150.5), /150,50/);
  assert.equal(dateLabel('2026-10-01'),'01/10/2026');
});
