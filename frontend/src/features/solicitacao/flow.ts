// Regras do formulário, separadas do visual para serem fáceis de testar.
export type Area = 'Hardware' | 'Redes' | 'Software';
export type Answers = Record<string, string>;
export interface Draft {
  version: 1;
  categoryId: number | null;
  area: Area | '';
  branches: Partial<Record<Area, Answers>>;
  mode: string;
  urgency: string;
  city: string;
  district: string;
  details: string;
}
export interface Question { key: string; title: string; options: string[] }
export const stages = ['Seu problema', 'Atendimento', 'Detalhes', 'Revisão'];
export const urgencies = ['Preciso resolver hoje', 'Próximos dias', 'Sem urgência'];
export const draftKey = 'techhelp.solicitacao.v1';
export const emptyDraft = (): Draft => ({ version: 1, categoryId: null, area: '', branches: {}, mode: '', urgency: '', city: '', district: '', details: '' });
export const answersFor = (draft: Draft): Answers => draft.area ? draft.branches[draft.area] ?? {} : {};

export function areaFor(name: string): Area | null {
  return (['Hardware', 'Redes', 'Software'] as const).find(area => area.toLowerCase() === name.trim().toLowerCase()) ?? null;
}

export function questionsFor(draft: Draft): Question[] {
  const a = answersFor(draft);
  const questions: Question[] = [];
  const add = (key: string, title: string, options: string[]) => questions.push({ key, title, options });
  if (draft.area === 'Hardware') {
    add('equipment', 'Qual equipamento precisa de ajuda?', ['Notebook', 'Computador de mesa', 'Outro', 'Não sei']);
    if (a.equipment) add('problem', 'O que está acontecendo?', ['Não liga', 'Liga, mas não apresenta imagem', 'Está muito lento', 'Superaquece', 'Tela quebrada', 'Problema de bateria', 'Problema de carregamento', 'Outro', 'Não sei identificar']);
    if (a.problem) add('brand', 'Você sabe a marca?', ['Dell', 'Lenovo', 'Acer', 'ASUS', 'Samsung', 'Apple', 'Outra', 'Não sei']);
    if (a.brand && a.problem === 'Está muito lento') add('access', 'Você consegue usar o equipamento e acessar a internet?', ['Sim', 'Não', 'Não sei']);
  }
  if (draft.area === 'Redes') {
    add('context', 'Onde você precisa de ajuda?', ['Casa', 'Empresa']);
    if (a.context) add('problem', 'O que você precisa resolver?', ['Internet lenta', 'Sem conexão', 'Sinal ruim em alguns lugares', 'Instalar ou organizar cabos', 'Configurar equipamento de rede', 'Outro', 'Não sei']);
    if (a.problem === 'Configurar equipamento de rede') add('device', 'Qual equipamento?', ['Roteador / Wi-Fi', 'Switch', 'Outro', 'Não sei']);
    else if (a.problem === 'Instalar ou organizar cabos') add('scope', 'O que precisa ser feito?', ['Instalar novos pontos', 'Reparar cabos existentes', 'Organizar a rede', 'Não sei']);
    else if (a.problem) add('scope', 'Isso acontece em quais aparelhos?', ['Em um aparelho', 'Em vários aparelhos', 'Em todos', 'Não sei']);
    if ((a.scope || a.device) && !['Sem conexão', 'Instalar ou organizar cabos', 'Sinal ruim em alguns lugares'].includes(a.problem)) {
      add('access', 'Você consegue usar algum computador com internet para receber ajuda?', ['Sim', 'Não', 'Não sei']);
    }
  }
  if (draft.area === 'Software') {
    add('problem', 'O que você precisa fazer?', ['Instalar ou configurar um programa', 'Resolver erro em um programa', 'Atualizar drivers', 'Fazer backup', 'Recuperar arquivos', 'Remover vírus ou malware', 'Formatar ou reinstalar o sistema', 'Outro', 'Não sei']);
    if (a.problem) add('system', 'Qual sistema você usa?', ['Windows', 'Linux', 'macOS', 'Outro', 'Não sei']);
    if (a.system && a.problem === 'Recuperar arquivos') add('files', 'O que aconteceu com os arquivos?', ['Foram apagados', 'O dispositivo não abre ou faz ruídos', 'Não sei']);
    if (a.system && a.problem === 'Fazer backup') add('backup', 'Você já tem onde guardar a cópia?', ['Disco externo', 'Conta na nuvem', 'Preciso de orientação', 'Não sei']);
    if (a.system && (!['Recuperar arquivos', 'Fazer backup'].includes(a.problem) || a.files || a.backup)) {
      add('access', 'O computador pode ser usado e tem acesso à internet?', ['Sim', 'Não', 'Não sei']);
    }
  }
  return questions;
}

export function remoteAllowed(draft: Draft): boolean {
  const a = answersFor(draft);
  if (a.access !== 'Sim') return false;
  if (draft.area === 'Hardware') return a.problem === 'Está muito lento';
  if (draft.area === 'Software') return a.problem !== 'Formatar ou reinstalar o sistema' && a.files !== 'O dispositivo não abre ou faz ruídos';
  return draft.area === 'Redes' && !['Sem conexão', 'Instalar ou organizar cabos', 'Sinal ruim em alguns lugares'].includes(a.problem);
}

export function selectAnswer(draft: Draft, key: string, value: string): Draft {
  if (!draft.area) return draft;
  const questions = questionsFor(draft);
  const index = questions.findIndex(question => question.key === key);
  if (index < 0 || !questions[index].options.includes(value)) return draft;
  const answers = { ...answersFor(draft) };
  if (answers[key] === value) return draft;
  // Uma nova escolha invalida só as respostas que dependem dela.
  delete answers[key + 'Text'];
  for (const question of questions.slice(index + 1)) {
    delete answers[question.key];
    delete answers[question.key + 'Text'];
  }
  if (key === 'problem') delete answers.programText;
  answers[key] = value;
  return { ...draft, branches: { ...draft.branches, [draft.area]: answers }, mode: '' };
}

export function validateStage(draft: Draft, stage: number): string | null {
  const questions = questionsFor(draft);
  const answers = answersFor(draft);
  if (stage === 0 && (!draft.categoryId || !questions.length || questions.some(q => !q.options.includes(answers[q.key])))) {
    return 'Escolha uma resposta para cada pergunta. “Não sei” também vale.';
  }
  if (stage === 1) {
    if (!['Presencial', 'Remoto'].includes(draft.mode) || (draft.mode === 'Remoto' && !remoteAllowed(draft))) return 'Escolha uma forma de atendimento disponível para seu relato.';
    if (!urgencies.includes(draft.urgency)) return 'Informe quando você precisa de ajuda.';
    if (draft.mode === 'Presencial' && (!draft.city.trim() || !draft.district.trim())) return 'Informe cidade/UF e bairro para o atendimento presencial.';
  }
  return null;
}

// O armazenamento do navegador pode conter dados antigos ou inválidos.
export function parseDraft(raw: string | null): Draft {
  try {
    const value = JSON.parse(raw ?? 'null');
    if (!value || value.version !== 1) return emptyDraft();
    const area = typeof value.area === 'string' ? areaFor(value.area) : null;
    const unassigned = value.area === '' && value.categoryId === null;
    if (!unassigned && (!area || !Number.isSafeInteger(value.categoryId) || value.categoryId <= 0)) return emptyDraft();
    const draft = emptyDraft();
    draft.area = area ?? '';
    draft.categoryId = value.categoryId;
    for (const field of ['mode', 'urgency', 'city', 'district', 'details'] as const) {
      draft[field] = typeof value[field] === 'string' ? value[field].slice(0, field === 'details' ? 3000 : 120) : '';
    }
    for (const area of ['Hardware', 'Redes', 'Software'] as const) {
      const source = value.branches?.[area];
      if (!source || typeof source !== 'object' || Array.isArray(source)) continue;
      const answers: Answers = {};
      const allowedKeys = ['equipment', 'problem', 'brand', 'access', 'context', 'device', 'scope', 'system', 'files', 'backup'];
      for (const key of [...allowedKeys, ...allowedKeys.map(k => k + 'Text'), 'programText']) {
        if (typeof source[key] === 'string') answers[key] = source[key].slice(0, 160);
      }
      draft.branches[area] = answers;
    }
    return draft;
  } catch { return emptyDraft(); }
}
