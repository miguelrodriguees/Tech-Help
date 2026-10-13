import { test } from 'node:test';
import assert from 'node:assert/strict';
import { api } from '../src/services/api.ts';
import { catalogPayload, editCatalogPayload, integerQuantity, isAdmin, saveCatalog } from '../src/features/catalogo/catalog.ts';

const admin = { idUsuario: 4, perfis: ['ADMIN'] };
test('edição aceita somente situações administráveis e preserva validação de valores', () => {
  assert.equal(editCatalogPayload(true, 'Chave', '', '5', 'MANUTENCAO', '4').quantidadeTotal, 4);
  assert.equal(editCatalogPayload(false, 'Kit', '', '5', 'INATIVO').status, 'INATIVO');
  assert.throws(() => editCatalogPayload(false, 'Kit', '', '5', 'INDISPONIVEL'));
  assert.throws(() => editCatalogPayload(true, 'Chave', '', '5', 'INATIVO', '4'));
});
test('edição usa PUT com CSRF, não repete após timeout e recusa sessão trocada', async () => {
  const original = api.defaults.adapter; const calls = []; let current = admin;
  api.defaults.adapter = async config => {
    calls.push(config);
    if (config.method === 'put') throw new Error('timeout');
    return { data: config.url === '/auth/me' ? current : { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    for (const kind of ['editTool', 'editKit']) {
      calls.length = 0;
      await assert.rejects(() => saveCatalog(admin, { kind, id: 7, body: { nome: 'Teste', valorDiaria: 5, status: 'DISPONIVEL' } }), /timeout/);
      const writes = calls.filter(c => c.method === 'put');
      assert.equal(writes.length, 1);
      assert.equal(writes[0].url, (kind === 'editTool' ? '/ferramentas/' : '/kits/') + '7');
      assert.equal(writes[0].headers.get('X-CSRF-TOKEN'), 'novo');
    }
    calls.length = 0; current = { ...admin, perfis: ['CLIENTE'] };
    await assert.rejects(() => saveCatalog(admin, { kind: 'editKit', id: 7, body: {} }));
    assert(calls.every(c => c.url === '/auth/me'));
  } finally { api.defaults.adapter = original; }
});
test('cadastro normaliza nome e diária e limita quantidades e precisão', () => {
  assert.deepEqual(catalogPayload(' Alicate ', ' Teste ', '25,50', '4'), { nome: 'Alicate', descricao: 'Teste', valorDiaria: 25.5, quantidadeTotal: 4 });
  assert.equal(catalogPayload('Kit', '', '0').valorDiaria, 0);
  for (const price of ['-1', '1.001', '', 'Infinity', '1e3', '10000000000']) assert.throws(() => catalogPayload('Kit', '', price));
  for (const quantity of ['-1', '1.5', '', '2147483648']) assert.throws(() => integerQuantity(quantity));
  assert.throws(() => integerQuantity('0'));
  assert.equal(integerQuantity('0', 0), 0);
  assert.throws(() => catalogPayload('   ', '', '10'));
});

test('administração não fica disponível a cliente ou visitante', () => {
  assert.equal(isAdmin(null), false);
  assert.equal(isAdmin({ perfis: ['CLIENTE', 'TECNICO'] }), false);
  assert.equal(isAdmin(admin), true);
});

test('escritas usam sessão atual e CSRF, inclusive remoção', async () => {
  const original = api.defaults.adapter;
  const calls = [];
  api.defaults.adapter = async config => {
    calls.push(config);
    return { data: config.url === '/auth/me' ? admin : { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    await saveCatalog(admin, { kind: 'component', kitId: 2, toolId: 7, quantity: 3 });
    assert.equal(calls.at(-1).url, '/kits/2/ferramentas');
    assert.deepEqual(JSON.parse(calls.at(-1).data), { idFerramenta: 7, quantidade: 3 });
    assert.equal(calls.at(-1).headers.get('X-CSRF-TOKEN'), 'novo');
    await saveCatalog(admin, { kind: 'remove', kitId: 2, toolId: 7 });
    assert.equal(calls.at(-1).method, 'delete');
    assert.equal(calls.at(-1).url, '/kits/2/ferramentas/7');
    assert.equal(calls.at(-1).headers.get('X-CSRF-TOKEN'), 'novo');
  } finally { api.defaults.adapter = original; }
});

test('troca de conta ou perda do perfil bloqueia qualquer escrita', async () => {
  const original = api.defaults.adapter;
  try {
    for (const current of [{ ...admin, idUsuario: 5 }, { ...admin, perfis: ['CLIENTE'] }, null]) {
      const calls = [];
      api.defaults.adapter = async config => {
        calls.push(config);
        return { data: current, status: 200, statusText: 'OK', headers: {}, config };
      };
      await assert.rejects(() => saveCatalog(admin, { kind: 'kit', body: catalogPayload('Kit', '', '10') }));
      assert.equal(calls.length, 1);
      assert.equal(calls[0].url, '/auth/me');
    }
  } finally { api.defaults.adapter = original; }
});

test('timeout de cadastro não repete POST automaticamente', async () => {
  const original = api.defaults.adapter;
  let writes = 0;
  api.defaults.adapter = async config => {
    if (config.method === 'post') { writes++; throw new Error('timeout'); }
    return { data: config.url === '/auth/me' ? admin : { headerName: 'X-CSRF-TOKEN', token: 'novo' }, status: 200, statusText: 'OK', headers: {}, config };
  };
  try {
    await assert.rejects(() => saveCatalog(admin, { kind: 'tool', body: catalogPayload('Alicate', '', '10', '2') }));
    assert.equal(writes, 1);
  } finally { api.defaults.adapter = original; }
});
