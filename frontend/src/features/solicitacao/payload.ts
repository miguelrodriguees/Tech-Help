// Converte somente as respostas visíveis. A identidade do cliente vem da sessão no servidor.
import { answersFor, questionsFor, type Answers, type Area, type Draft } from './flow.ts';

export interface SolicitacaoPayload {
  idCategoria: number;
  titulo: string;
  descricao: string;
  tipoAtendimento: 'PRESENCIAL' | 'REMOTO';
  urgencia: 'BAIXA' | 'NORMAL' | 'ALTA';
}

export type PayloadResult =
  | { ok: true; payload: SolicitacaoPayload }
  | { ok: false; reason: string };

export interface SummaryRow { label: string; value: string }

/** Rótulos legíveis para cada resposta do fluxo. */
const labels: Record<string, string> = {
  equipment: 'Equipamento',
  problem: 'Problema relatado',
  brand: 'Marca',
  access: 'Consegue usar o equipamento com internet',
  context: 'Local',
  device: 'Equipamento de rede',
  scope: 'Abrangência',
  system: 'Sistema',
  files: 'Situação dos arquivos',
  backup: 'Onde guardar a cópia',
};

const problemLabel: Record<Area, string> = {
  Hardware: 'Problema relatado',
  Redes: 'Necessidade',
  Software: 'Necessidade',
};

/**
 * Mapeamento de urgência. CANDIDATO, AINDA A VALIDAR como regra de produto.
 * "Hoje" vira ALTA e nunca EMERGENCIA: a plataforma não promete disponibilidade.
 */
export const urgencyMap: Record<string, 'BAIXA' | 'NORMAL' | 'ALTA'> = {
  'Preciso resolver hoje': 'ALTA',
  'Próximos dias': 'NORMAL',
  'Sem urgência': 'BAIXA',
};

function lowerFirst(text: string): string {
  return text ? text[0].toLowerCase() + text.slice(1) : text;
}

function withExtra(answers: Answers, key: string): string {
  const value = answers[key] ?? '';
  const extra = (answers[key + 'Text'] ?? '').trim();
  return extra ? `${value} — ${extra}` : value;
}

/** Título legível a partir das respostas visíveis. Máximo de 160 caracteres. */
export function buildTitle(draft: Draft): string {
  const a = answersFor(draft);
  const known = (value?: string) => (value && !value.startsWith('Não sei') && value !== 'Outro' && value !== 'Outra' ? value : '');
  let title = '';

  if (draft.area === 'Hardware') {
    const parts = [known(a.equipment), known(a.brand)].filter(Boolean).join(' ');
    title = parts ? `${parts} ${lowerFirst(a.problem ?? '')}`.trim() : (a.problem ?? '');
  } else if (draft.area === 'Redes') {
    const local = known(a.context);
    title = local ? `Rede em ${local.toLowerCase()}: ${lowerFirst(a.problem ?? '')}` : `Rede: ${lowerFirst(a.problem ?? '')}`;
  } else if (draft.area === 'Software') {
    const system = known(a.system);
    title = system ? `${a.problem ?? ''} (${system})` : (a.problem ?? '');
  }

  title = title.replace(/\s+/g, ' ').trim();
  if (!title) title = draft.area || 'Pedido de atendimento';
  return title.length > 160 ? title.slice(0, 157).trimEnd() + '…' : title;
}

/**
 * Resumo do pedido, na ordem em que as perguntas apareceram.
 * Só considera as perguntas visíveis, então uma resposta antiga escondida
 * nunca vaza para a descrição.
 */
export function summarizeDraft(draft: Draft): SummaryRow[] {
  const a = answersFor(draft);
  const rows: SummaryRow[] = [];

  for (const question of questionsFor(draft)) {
    const value = withExtra(a, question.key);
    if (!value) continue;
    const label = question.key === 'problem' && draft.area ? problemLabel[draft.area] : labels[question.key] ?? question.title;
    rows.push({ label, value });
  }

  const program = (a.programText ?? '').trim();
  if (program) rows.push({ label: 'Programa', value: program });

  if (draft.mode) rows.push({ label: 'Atendimento', value: draft.mode });
  if (draft.urgency) rows.push({ label: 'Urgência informada', value: draft.urgency });

  if (draft.mode === 'Presencial') {
    const region = [draft.city.trim(), draft.district.trim()].filter(Boolean).join(' — ');
    if (region) rows.push({ label: 'Região informada', value: region });
  }

  const details = draft.details.trim();
  if (details) rows.push({ label: 'Detalhes', value: details });

  return rows;
}

/** Descrição estruturada em texto, a partir do mesmo resumo da revisão. */
export function buildDescricao(draft: Draft): string {
  return summarizeDraft(draft)
    .map(row => `${row.label}: ${row.value}`)
    .join('\n');
}

/**
 * Monta o corpo de POST /solicitacoes.
 *
 * Cidade e bairro digitados NÃO são um idEndereco: o campo é omitido de
 * propósito. Ligar endereço estruturado exige decisão separada, porque o
 * backend valida se o endereço pertence ao usuário do cliente.
 */
export function toSolicitacaoPayload(draft: Draft): PayloadResult {
  if (!draft.area || !draft.categoryId) {
    return { ok: false, reason: 'Escolha uma área de atendimento.' };
  }
  const tipo = draft.mode === 'Presencial' ? 'PRESENCIAL' : draft.mode === 'Remoto' ? 'REMOTO' : null;
  if (!tipo) {
    return { ok: false, reason: 'Escolha uma forma de atendimento.' };
  }
  const urgencia = urgencyMap[draft.urgency];
  if (!urgencia) {
    return { ok: false, reason: 'Informe quando você precisa de ajuda.' };
  }
  const descricao = buildDescricao(draft);
  if (!descricao.trim()) {
    return { ok: false, reason: 'Responda as perguntas do pedido antes de publicar.' };
  }

  return {
    ok: true,
    payload: {
      idCategoria: draft.categoryId,
      titulo: buildTitle(draft),
      descricao,
      tipoAtendimento: tipo,
      urgencia,
    },
  };
}
