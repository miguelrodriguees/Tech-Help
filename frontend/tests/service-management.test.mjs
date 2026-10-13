import assert from 'node:assert/strict';
import { test } from 'node:test';
import { api } from '../src/services/api.ts';
import { canCancel, schedulePayload, executeService } from '../src/features/servicos/services.ts';

const client = { idUsuario: 1, idCliente: 10, idTecnico: null, perfis: ['CLIENTE'] };
const tech = { idUsuario: 2, idCliente: null, idTecnico: 20, perfis: ['TECNICO'] };
const service = { idServico: 300, idCliente: 10, idTecnico: 20, status: 'AGENDADO' };

test('cancelamento disponível somente aos participantes antes do início', () => {
  assert(canCancel(service, client));
  assert(canCancel(service, tech));
  assert.equal(canCancel(service, { ...client, idCliente: 11 }), false);
  assert.equal(canCancel(service, { ...tech, idTecnico: 21 }), false);
  for (const status of ['EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO']) assert.equal(canCancel({ ...service, status }, client), false);
});

test('agendamento preserva horário informado e rejeita datas impossíveis', () => {
  assert.deepEqual(schedulePayload('2099-05-20T14:30'), { dataAgendada: '2099-05-20T14:30:00' });
  for (const value of ['', '2099-02-30T14:30', '2099-05-20T25:30', '2099-05-20T14:30Z']) assert.throws(() => schedulePayload(value));
});

test('cliente só cancela; técnico agenda com CSRF e não envia após troca de conta', async () => {
  const original = api.defaults.adapter;
  let current = client;
  const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    return { data: config.url === '/auth/me' ? current : config.url === '/auth/csrf' ? { headerName: 'X-CSRF-TOKEN', token: 'novo' } : service, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    await assert.rejects(() => executeService(service, 'agendar', client, '2099-05-20T14:30'));
    assert(calls.every(c => c.method === 'get'));
    calls.length = 0;
    await executeService(service, 'cancelar', client);
    assert.equal(calls.at(-1).url, '/servicos/300/cancelar');
    assert.equal(calls.at(-1).headers.get('X-CSRF-TOKEN'), 'novo');
    current = tech; calls.length = 0;
    await executeService(service, 'agendar', tech, '2099-05-20T14:30');
    assert.equal(calls.at(-1).url, '/servicos/300/agendar');
    assert.deepEqual(JSON.parse(calls.at(-1).data), { dataAgendada: '2099-05-20T14:30:00' });
    current = client; calls.length = 0;
    await assert.rejects(() => executeService(service, 'cancelar', tech));
    assert(calls.every(c => c.method === 'get'));
  } finally { api.defaults.adapter = original; }
});
