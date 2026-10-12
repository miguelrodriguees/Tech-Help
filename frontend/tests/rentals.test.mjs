import assert from 'node:assert/strict';
import { test } from 'node:test';
import { api } from '../src/services/api.ts';
import { reservationPayload, estimate, reserve, cancelRental } from '../src/features/aluguel/rentals.ts';
const session = { idUsuario: 1 };
test('reserva valida quantidade, calendário e ordem das datas sem converter o horário', () => {
  const body = reservationPayload(session, 2, '3', '2099-01-01T10:00', '2099-01-03T10:00');
  assert.equal(body.dataPrevistaRetirada, '2099-01-01T10:00:00');
  assert.equal(body.quantidade, 3);
  assert.deepEqual(estimate({ valorDiaria: 5 }, 3, '2099-01-01', '2099-01-03'), { days: 2, total: 30 });
  assert.equal(estimate({ valorDiaria: 5 }, 1, '2099-01-01', '2099-01-01').total, 5);
  for (const value of ['0', '-1', '1.5']) assert.throws(() => reservationPayload(session, 2, value, '2099-01-01T10:00', '2099-01-03T10:00'));
  assert.throws(() => reservationPayload(session, 2, '1', '2099-02-30T10:00', '2099-03-01T10:00'));
  assert.throws(() => reservationPayload(session, 2, '1', '2099-01-03T10:00', '2099-01-01T10:00'));
});
test('reserva envia uma operação com CSRF e não repete após timeout; sessão trocada bloqueia escrita', async () => {
  const original = api.defaults.adapter; const calls = []; let current = session;
  api.defaults.adapter = async config => {
    calls.push(config);
    if (config.method === 'post') throw new Error('timeout');
    return { data: config.url === '/auth/me' ? current : { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    const body = reservationPayload(session, 2, '1', '2099-01-01T10:00', '2099-01-03T10:00');
    await assert.rejects(() => reserve(session, body));
    assert.equal(calls.filter(c => c.method === 'post').length, 1);
    assert.equal(calls.at(-1).url, '/alugueis/reservar');
    assert.equal(calls.at(-1).headers.get('X-CSRF-TOKEN'), 'novo');
    current = { idUsuario: 2 }; calls.length = 0;
    await assert.rejects(() => reserve(session, body));
    assert(calls.every(c => c.method === 'get'));
    current = session;
    await assert.rejects(() => cancelRental(session, { idUsuario: 2, status: 'RESERVADO' }));
    await assert.rejects(() => cancelRental(session, { idUsuario: 1, status: 'RETIRADO' }));
  } finally { api.defaults.adapter = original; }
});
