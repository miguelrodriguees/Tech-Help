import assert from 'node:assert/strict';
import { test } from 'node:test';
import { emptyDraft, parseDraft, questionsFor, remoteAllowed, selectAnswer, validateStage } from '../src/features/solicitacao/flow.ts';

const hardware = () => ({ ...emptyDraft(), categoryId: 1, area: 'Hardware' });
const answer = (draft, entries) => entries.reduce((state, [key, value]) => selectAnswer(state, key, value), draft);

test('Notebook com tela preta: perguntas progressivas e presencial obrigatório', () => {
  let draft = hardware();
  assert.deepEqual(questionsFor(draft).map(q => q.key), ['equipment']);
  assert(validateStage(draft, 0));
  draft = answer(draft, [['equipment', 'Notebook'], ['problem', 'Liga, mas não apresenta imagem'], ['brand', 'Dell']]);
  assert.equal(validateStage(draft, 0), null);
  assert.equal(remoteAllowed(draft), false);
  assert(validateStage({ ...draft, mode: 'Remoto', urgency: 'Próximos dias' }, 1));
  assert(validateStage({ ...draft, mode: 'Presencial', urgency: 'Próximos dias' }, 1));
  assert.equal(validateStage({ ...draft, mode: 'Presencial', urgency: 'Próximos dias', city: 'Taboão da Serra, SP', district: 'Centro' }, 1), null);
});

test('Editar resposta invalida dependências e atendimento, mas preserva região e detalhes', () => {
  let draft = answer(hardware(), [['equipment', 'Notebook'], ['problem', 'Está muito lento'], ['brand', 'Dell'], ['access', 'Sim']]);
  assert(remoteAllowed(draft));
  draft = { ...draft, mode: 'Remoto', city: 'São Paulo, SP', details: 'Começou ontem.' };
  draft = selectAnswer(draft, 'problem', 'Tela quebrada');
  assert.equal(draft.mode, '');
  assert.equal(draft.branches.Hardware.brand, undefined);
  assert.equal(draft.branches.Hardware.access, undefined);
  assert.equal(draft.city, 'São Paulo, SP');
  assert.equal(draft.details, 'Começou ontem.');
  assert.equal(remoteAllowed(draft), false);
});

test('Redes não pede marca; falta de conexão impede remoto', () => {
  const draft = answer({ ...emptyDraft(), area: 'Redes', categoryId: 3 }, [['context', 'Casa'], ['problem', 'Sem conexão'], ['scope', 'Em todos']]);
  assert.equal(validateStage(draft, 0), null);
  assert.equal(remoteAllowed(draft), false);
  assert(!questionsFor(draft).some(q => q.key === 'brand'));
});

test('Software libera remoto quando utilizável, mas não para possível falha física', () => {
  let draft = answer({ ...emptyDraft(), area: 'Software', categoryId: 2 }, [['problem', 'Recuperar arquivos'], ['system', 'Windows'], ['files', 'O dispositivo não abre ou faz ruídos'], ['access', 'Sim']]);
  assert.equal(remoteAllowed(draft), false);
  draft = answer(draft, [['problem', 'Instalar ou configurar um programa'], ['system', 'Linux'], ['access', 'Sim']]);
  assert(remoteAllowed(draft));
  assert.equal(validateStage({ ...draft, mode: 'Remoto', urgency: 'Sem urgência' }, 1), null);
});

test('Outro e Não sei são válidos; edição apaga texto dependente antigo', () => {
  let draft = answer(hardware(), [['equipment', 'Outro'], ['problem', 'Não sei identificar'], ['brand', 'Outra']]);
  draft.branches.Hardware.brandText = 'Marca anterior';
  assert.equal(validateStage(draft, 0), null);
  draft = selectAnswer(draft, 'equipment', 'Notebook');
  assert.equal(draft.branches.Hardware.brandText, undefined);
  assert.equal(selectAnswer(draft, 'equipment', 'Opção inventada'), draft);
});

test('Rascunho sobrevive a serialização; conteúdo inválido não quebra a página', () => {
  const draft = answer(hardware(), [['equipment', 'Notebook'], ['problem', 'Não liga'], ['brand', 'Não sei']]);
  draft.details = 'Luz não acende.';
  assert.deepEqual(parseDraft(JSON.stringify(draft)), draft);
  const unsure = { ...emptyDraft(), details: 'Não sei qual categoria escolher.' };
  assert.deepEqual(parseDraft(JSON.stringify(unsure)), unsure);
  for (const raw of [null, '{', '{}', '{"version":99}', '{"version":1,"area":{},"categoryId":1}']) assert.deepEqual(parseDraft(raw), emptyDraft());
});
