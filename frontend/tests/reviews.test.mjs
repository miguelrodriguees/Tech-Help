import assert from 'node:assert/strict';
import { test } from 'node:test';
import { AxiosError } from 'axios';
import { api } from '../src/services/api.ts';
import { reviewPayload, submitReview } from '../src/features/servicos/reviews.ts';

test('avaliação exige nota inteira de 1 a 5 e limita comentário sem enviar identidades', () => {
  for (const nota of [0, 6, 2.5, NaN]) assert.throws(() => reviewPayload(nota, ''));
  assert.throws(() => reviewPayload(5, 'a'.repeat(3001)));
  assert.deepEqual(reviewPayload(5, '  Resolvido  '), { nota: 5, comentario: 'Resolvido' });
  assert.deepEqual(reviewPayload(1, '  '), { nota: 1, comentario: null });
  assert.equal(reviewPayload(3, 'a'.repeat(3000)).comentario.length, 3000);
});

test('envio confirma cliente, conclusão, CSRF e não repete POST após timeout', async () => {
  const original = api.defaults.adapter;
  const expected = { idUsuario: 1, idCliente: 10, perfis: ['CLIENTE'] };
  const service = { idServico: 300, idCliente: 10, status: 'CONCLUIDO' };
  let current = expected;
  let fail = false;
  const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    if (fail && config.method === 'post') throw new AxiosError('timeout', 'ECONNABORTED', config);
    return { data: config.url === '/auth/me' ? current : config.url === '/auth/csrf' ? { headerName: 'X-CSRF-TOKEN', token: 'novo' } : { idAvaliacao: 7, nota: 5 }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    for (const changed of [null, { ...expected, idUsuario: 2 }, { ...expected, idCliente: 20 }, { ...expected, perfis: ['TECNICO'] }]) {
      current = changed;
      await assert.rejects(() => submitReview(service, expected, 5, ''));
    }
    current = expected;
    await assert.rejects(() => submitReview({ ...service, status: 'EM_ANDAMENTO' }, expected, 5, ''));
    assert(calls.every(c => c.method === 'get' && c.url === '/auth/me'));
    calls.length = 0;
    assert.equal((await submitReview(service, expected, 5, 'Resolvido')).idAvaliacao, 7);
    assert.deepEqual(calls.map(c => c.url), ['/auth/me', '/auth/csrf', '/avaliacoes/servico/300/cliente']);
    assert.deepEqual(JSON.parse(calls[2].data), { nota: 5, comentario: 'Resolvido' });
    assert.equal(calls[2].headers.get('X-CSRF-TOKEN'), 'novo');
    assert.equal(calls[2].withCredentials, true);
    calls.length = 0; fail = true;
    await assert.rejects(() => submitReview(service, expected, 5, ''));
    assert.equal(calls.filter(c => c.method === 'post').length, 1);
  } finally { api.defaults.adapter = original; }
});
