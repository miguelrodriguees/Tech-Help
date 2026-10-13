import { answersFor, questionsFor, validateStage, type Draft } from './flow.ts';

export function requestPayload(draft: Draft, idCliente: number) {
  if (!Number.isSafeInteger(idCliente) || idCliente <= 0) throw new Error('Entre com uma conta de cliente para publicar.');
  for (const stage of [0, 1]) {
    const error = validateStage(draft, stage);
    if (error) throw new Error(error);
  }
  const answers = answersFor(draft);
  const description = questionsFor(draft).map(q =>
    `${q.title}\n${answers[q.key]}${answers[q.key + 'Text'] ? ` — ${answers[q.key + 'Text']}` : ''}`);
  if (answers.programText && answers.problem?.includes('programa')) description.push(`Programa: ${answers.programText}`);
  if (draft.mode === 'Presencial') description.push(`Região do atendimento: ${draft.city.trim()} — ${draft.district.trim()}`);
  if (draft.details.trim()) description.push(`Detalhes: ${draft.details.trim()}`);
  return {
    idCliente,
    idCategoria: draft.categoryId,
    titulo: `${draft.area}: ${answers.problem}`.slice(0, 160),
    descricao: description.join('\n\n'),
    tipoAtendimento: draft.mode === 'Remoto' ? 'REMOTO' : 'PRESENCIAL',
    urgencia: draft.urgency === 'Preciso resolver hoje' ? 'ALTA' : draft.urgency === 'Sem urgência' ? 'BAIXA' : 'NORMAL',
  };
}
