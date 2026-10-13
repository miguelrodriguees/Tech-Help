import assert from 'node:assert/strict';
import { test } from 'node:test';
import { api } from '../src/services/api.ts';
import { loadTechnicianNames, technicianLabel } from '../src/features/propostas/technicians.ts';

test('nome público mantém identificação por número quando indisponível', () => {
  assert.equal(technicianLabel(20, { 20: 'Ana Silva' }), 'Ana Silva');
  assert.equal(technicianLabel(20, {}), 'Técnico #20');
  assert.equal(technicianLabel(20, { 20: ' ' }), 'Técnico #20');
});

test('consulta nomes em lotes sem repetir técnicos e preserva resultados de lotes disponíveis', async () => {
  const original = api.defaults.adapter;
  const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    const ids = config.params.ids.split(',').map(Number);
    if (ids.includes(51)) throw new Error('indisponível');
    return { data: [...ids.map(idTecnico => ({ idTecnico, nome: `Nome ${idTecnico}` })), { idTecnico: 999, nome: 'Fora da consulta' }], status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    assert.deepEqual(await loadTechnicianNames([]), {});
    assert.equal(calls.length, 0);
    const names = await loadTechnicianNames([...Array.from({ length: 51 }, (_, i) => i + 1), 1, 1, -3, NaN]);
    assert.equal(calls.length, 2);
    assert(calls.every(c => c.url === '/tecnicos/resumos' && c.method === 'get'));
    assert.equal(calls[0].params.ids.split(',').length, 50);
    assert.equal(calls[1].params.ids, '51');
    assert.equal(names[1], 'Nome 1');
    assert.equal(names[999], undefined);
    assert.equal(technicianLabel(51, names), 'Técnico #51');
  } finally { api.defaults.adapter = original; }
});
