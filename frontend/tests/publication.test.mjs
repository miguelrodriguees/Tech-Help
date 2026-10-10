import assert from 'node:assert/strict';
import { test } from 'node:test';
import { emptyDraft } from '../src/features/solicitacao/flow.ts';
import { requestPayload } from '../src/features/solicitacao/publication.ts';

const draft = () => ({ ...emptyDraft(), area: 'Hardware', categoryId: 1,
  branches: { Hardware: { equipment: 'Notebook', problem: 'Não liga', brand: 'Dell' }, Software: { problem: 'Resposta antiga' } },
  mode: 'Presencial', urgency: 'Preciso resolver hoje', city: 'São Paulo, SP', district: 'Centro', details: 'Começou ontem.' });

test('publicação preserva respostas e região usando identidade da sessão', () => {
  const original = draft();
  const payload = requestPayload(original, 42);
  assert.equal(payload.idCliente, 42);
  assert.equal(payload.idCategoria, 1);
  assert.equal(payload.titulo, 'Hardware: Não liga');
  assert.equal(payload.tipoAtendimento, 'PRESENCIAL');
  assert.equal(payload.urgencia, 'ALTA');
  assert.match(payload.descricao, /Notebook/);
  assert.match(payload.descricao, /São Paulo, SP — Centro/);
  assert.match(payload.descricao, /Começou ontem/);
  assert.doesNotMatch(payload.descricao, /Resposta antiga/);
  assert.equal(payload.idEndereco, undefined);
  assert.deepEqual(original, draft());
});

test('remoto não envia localização antiga e converte urgências', () => {
  const remote = { ...draft(), mode: 'Remoto', urgency: 'Sem urgência',
    branches: { Hardware: { equipment: 'Notebook', problem: 'Está muito lento', brand: 'Dell', access: 'Sim' } } };
  assert.equal(requestPayload(remote, 42).urgencia, 'BAIXA');
  assert.equal(requestPayload(remote, 42).tipoAtendimento, 'REMOTO');
  assert.doesNotMatch(requestPayload(remote, 42).descricao, /São Paulo/);
  assert.equal(requestPayload({ ...remote, urgency: 'Próximos dias' }, 42).urgencia, 'NORMAL');
});

test('impede publicação incompleta, sem cliente ou com remoto incompatível', () => {
  assert.throws(() => requestPayload(emptyDraft(), 42));
  for (const id of [null, 0, -1, NaN]) assert.throws(() => requestPayload(draft(), id));
  assert.throws(() => requestPayload({ ...draft(), mode: 'Remoto' }, 42));
  assert.throws(() => requestPayload({ ...draft(), city: '' }, 42));
});
