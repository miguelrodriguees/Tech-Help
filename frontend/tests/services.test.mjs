import assert from 'node:assert/strict';
import { test } from 'node:test';
import { AxiosError } from 'axios';
import { api } from '../src/services/api.ts';
import { availableAction, executeService, serviceDate, serviceStatus, serviceViews } from '../src/features/servicos/services.ts';

const technician = { idUsuario: 2, idTecnico: 20, idCliente: null, perfis: ['TECNICO'] };
const client = { idUsuario: 1, idCliente: 10, idTecnico: null, perfis: ['CLIENTE'] };
const service = { idServico: 300, idTecnico: 20, idCliente: 10, status: 'AGENDADO' };

test('cliente acompanha; somente técnico responsável tem ações de execução', () => {
  assert.equal(availableAction(service, client), null);
  assert.equal(availableAction(service, { ...technician, idTecnico: 21 }), null);
  assert.equal(availableAction(service, { ...technician, perfis: [] }), null);
  assert.equal(availableAction(service, technician), 'iniciar');
  assert.equal(availableAction({ ...service, status: 'EM_ANDAMENTO' }, technician), 'concluir');
  for (const status of ['CONCLUIDO', 'CANCELADO', 'DESCONHECIDO']) assert.equal(availableAction({ ...service, status }, technician), null);
  assert.deepEqual(serviceViews(client), ['cliente']);
  assert.deepEqual(serviceViews(technician), ['tecnico']);
  assert.deepEqual(serviceViews({ ...technician, idCliente: 10, perfis: ['CLIENTE', 'TECNICO'] }), ['cliente', 'tecnico']);
});

test('aceite não inventa agendamento; datas mantêm horário do backend', () => {
  assert.equal(serviceStatus('AGENDADO'), 'Aguardando início');
  assert.equal(serviceDate(null, 'Ainda não informada'), 'Ainda não informada');
  assert.equal(serviceDate('2026-10-09T00:15:34.123', ''), '09/10/2026 às 00:15');
});

test('execução revalida identidade, usa CSRF e não repete escrita após timeout', async () => {
  const original = api.defaults.adapter;
  let current = technician;
  let fail = false;
  const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    if (fail && config.method === 'post') throw new AxiosError('timeout', 'ECONNABORTED', config);
    return { data: config.url === '/auth/me' ? current : config.url === '/auth/csrf' ? { headerName: 'X-CSRF-TOKEN', token: 'novo' } : { ...service, status: 'EM_ANDAMENTO' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    for (const changed of [null, client, { ...technician, idUsuario: 3 }, { ...technician, perfis: [] }]) {
      current = changed;
      await assert.rejects(() => executeService(service, 'iniciar', technician));
    }
    assert(calls.every(c => c.method === 'get' && c.url === '/auth/me'));
    current = technician; calls.length = 0;
    assert.equal((await executeService(service, 'iniciar', technician)).status, 'EM_ANDAMENTO');
    assert.deepEqual(calls.map(c => c.url), ['/auth/me', '/auth/csrf', '/servicos/300/iniciar']);
    assert.equal(calls[2].headers.get('X-CSRF-TOKEN'), 'novo');
    assert.equal(calls[2].withCredentials, true);
    calls.length = 0;
    await assert.rejects(() => executeService(service, 'concluir', technician));
    assert(calls.every(c => c.method === 'get'));
    calls.length = 0; fail = true;
    await assert.rejects(() => executeService({ ...service, status: 'EM_ANDAMENTO' }, 'concluir', technician));
    assert.equal(calls.filter(c => c.method === 'post').length, 1);
    assert.equal(calls.at(-1).url, '/servicos/300/concluir');
  } finally { api.defaults.adapter = original; }
});
