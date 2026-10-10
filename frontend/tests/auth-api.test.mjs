import assert from 'node:assert/strict';
import { test } from 'node:test';
import { AxiosError } from 'axios';
import { api, getSession, postProtected } from '../src/services/api.ts';

test('escritas enviam cookie, token novo e formato correto sem repetir POST', async () => {
  const original = api.defaults.adapter;
  const calls = [];
  let token = 0;
  api.defaults.adapter = async config => {
    calls.push(config);
    return { data: config.method === 'get' ? { headerName: 'X-CSRF-TOKEN', token: `token-${++token}` } : {}, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    await postProtected('/auth/login', new URLSearchParams({ email: 'cliente@example.invalid', senha: 'Teste123!' }));
    await postProtected('/solicitacoes', { idCliente: 42 });
    assert.deepEqual(calls.map(c => c.url), ['/auth/csrf', '/auth/login', '/auth/csrf', '/solicitacoes']);
    assert(calls.every(c => c.withCredentials));
    assert.equal(calls[1].headers.get('X-CSRF-TOKEN'), 'token-1');
    assert.equal(calls[3].headers.get('X-CSRF-TOKEN'), 'token-2');
    assert.match(calls[1].headers.get('Content-Type'), /application\/x-www-form-urlencoded/);
    assert.match(calls[3].headers.get('Content-Type'), /application\/json/);
    let posts = 0;
    api.defaults.adapter = async config => {
      if (config.method === 'post') { posts++; throw new AxiosError('timeout', 'ECONNABORTED', config); }
      return { data: { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
    };
    await assert.rejects(() => postProtected('/solicitacoes', {}));
    assert.equal(posts, 1);
  } finally { api.defaults.adapter = original; }
});

test('sessão ausente difere de indisponibilidade do servidor', async () => {
  const original = api.defaults.adapter;
  try {
    api.defaults.adapter = async config => { throw new AxiosError('unauthorized', '', config, {}, { status: 401, data: {} }); };
    assert.equal(await getSession(), null);
    api.defaults.adapter = async config => { throw new AxiosError('offline', 'ERR_NETWORK', config); };
    await assert.rejects(getSession);
  } finally { api.defaults.adapter = original; }
});
