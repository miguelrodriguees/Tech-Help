export interface Proposal {
  idProposta: number;
  idSolicitacao: number;
  idTecnico: number;
  valor: number;
  mensagem: string | null;
  prazoEstimadoDias: number | null;
  dataDisponivel: string | null;
  status: string;
}

export const money = (value: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
export const openForProposals = (status: string) => ['ABERTA', 'EM_NEGOCIACAO'].includes(status);
export const statusLabel = (status: string) => ({ ENVIADA: 'Enviada', ACEITA: 'Aceita', RECUSADA: 'Recusada', ABERTA: 'Aberta', EM_NEGOCIACAO: 'Em negociação', CONTRATADA: 'Contratada', CONCLUIDA: 'Concluída', CANCELADA: 'Cancelada' }[status] ?? status.replaceAll('_', ' '));
export const dateLabel = (value: string | null) => value ? value.split('-').reverse().join('/') : 'A combinar';

export function proposalPayload(idSolicitacao: number, idTecnico: number, fields: { valor: string; mensagem: string; prazo: string; data: string }) {
  if (![idSolicitacao, idTecnico].every(id => Number.isSafeInteger(id) && id > 0)) throw new Error('Solicitação ou conta de técnico inválida.');
  const raw = fields.valor.trim().replace(',', '.');
  if (!/^\d{1,10}(\.\d{1,2})?$/.test(raw) || Number(raw) <= 0) throw new Error('Informe um valor maior que zero, com até duas casas decimais.');
  const prazo = fields.prazo.trim();
  if (prazo && (!/^\d+$/.test(prazo) || Number(prazo) > 32767)) throw new Error('Informe o prazo em dias inteiros, entre 0 e 32767.');
  return { idSolicitacao, idTecnico, valor: raw, mensagem: fields.mensagem.trim() || null, prazoEstimadoDias: prazo === '' ? null : Number(prazo), dataDisponivel: fields.data || null };
}
