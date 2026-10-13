import assert from 'node:assert/strict';
import { test } from 'node:test';
import { api } from '../src/services/api.ts';
import { loadAdminRentals, manageRental, overdue } from '../src/features/aluguel/admin-rentals.ts';
const admin = { idUsuario: 4, perfis: ['ADMIN'] };
test('admin reconfirma identidade e perfil; cliente e sessão trocada não podem listar ou registrar', async () => {
  const original = api.defaults.adapter; let current = admin; const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    return { data: config.url === '/auth/me' ? current : {}, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    const rental = { idAluguel: 5, status: 'RESERVADO' };
    for (const value of [{ idUsuario: 4, perfis: ['CLIENTE'] }, { idUsuario: 9, perfis: ['ADMIN'] }, null]) {
      current = value; calls.length = 0;
      await assert.rejects(() => loadAdminRentals(admin, 0, new AbortController().signal));
      await assert.rejects(() => manageRental(admin, rental, 'retirar'));
      assert(calls.every(c => c.url === '/auth/me'));
    }
    current = admin;
    await assert.rejects(() => manageRental({ idUsuario: 4, perfis: ['CLIENTE'] }, rental, 'retirar'));
  } finally { api.defaults.adapter = original; }
});
test('transições administrativas validam estado, usam CSRF e não repetem escrita após falha', async () => {
  const original = api.defaults.adapter; const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    if (config.method === 'post') throw new Error('timeout');
    return { data: config.url === '/auth/me' ? admin : { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    for (const [action, status] of [['retirar', 'RESERVADO'], ['devolver', 'RETIRADO'], ['devolver', 'ATRASADO']]) {
      calls.length = 0;
      await assert.rejects(() => manageRental(admin, { idAluguel: 5, status }, action), /timeout/);
      const writes = calls.filter(c => c.method === 'post');
      assert.equal(writes.length, 1); assert.equal(writes[0].url, '/alugueis/5/' + action);
      assert.equal(writes[0].headers.get('X-CSRF-TOKEN'), 'novo');
    }
    for (const [action, status] of [['devolver', 'RESERVADO'], ['retirar', 'RETIRADO'], ['devolver', 'DEVOLVIDO'], ['retirar', 'CANCELADO']]) {
      calls.length = 0;
      await assert.rejects(() => manageRental(admin, { idAluguel: 5, status }, action));
      assert(calls.every(c => c.url === '/auth/me'));
    }
  } finally { api.defaults.adapter = original; }
});
test('aviso de atraso considera Brasília e somente equipamento ainda retirado', () => {
  const now = new Date('2026-10-14T13:00:01Z');
  const rental = { status: 'RETIRADO', dataPrevistaDevolucao: '2026-10-14T10:00:00' };
  assert.equal(overdue(rental, now), true);
  assert.equal(overdue(rental, new Date('2026-10-14T13:00:00Z')), false);
  assert.equal(overdue({ ...rental, status: 'DEVOLVIDO' }, now), false);
  assert.equal(overdue({ ...rental, status: 'RESERVADO' }, now), false);
});
