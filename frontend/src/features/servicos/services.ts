import { getSession, postProtected, type Session } from '../../services/api.ts';

export interface Service {
  idServico: number;
  idProposta: number;
  idCliente: number;
  idTecnico: number;
  valorAcordado: number;
  status: string;
  dataAgendada: string | null;
  dataInicio: string | null;
  dataConclusao: string | null;
}
export type ServiceView = 'cliente' | 'tecnico';
export type ServiceAction = 'iniciar' | 'concluir' | 'agendar' | 'cancelar';

export function serviceViews(session: Session): ServiceView[] {
  const views: ServiceView[] = [];
  if (session.idCliente && session.perfis.includes('CLIENTE')) views.push('cliente');
  if (session.idTecnico && session.perfis.includes('TECNICO')) views.push('tecnico');
  return views;
}

export function availableAction(service: Service, session: Session): ServiceAction | null {
  if (!session.perfis.includes('TECNICO') || session.idTecnico !== service.idTecnico) return null;
  return service.status === 'AGENDADO' ? 'iniciar' : service.status === 'EM_ANDAMENTO' ? 'concluir' : null;
}

export function canCancel(service: Service, session: Session): boolean {
  return service.status === 'AGENDADO' && (
    (session.perfis.includes('CLIENTE') && session.idCliente === service.idCliente) ||
    (session.perfis.includes('TECNICO') && session.idTecnico === service.idTecnico));
}

export function schedulePayload(value: string) {
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) throw new Error('Informe dia e horário válidos.');
  const date = new Date(value + ':00Z');
  if (!Number.isFinite(date.getTime()) || date.toISOString().slice(0, 16) !== value) throw new Error('Informe uma data válida.');
  return { dataAgendada: value + ':00' };
}

export const serviceStatus = (status: string) => ({ AGENDADO: 'Aguardando início', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído', CANCELADO: 'Cancelado' }[status] ?? status.replaceAll('_', ' '));

// O backend envia LocalDateTime sem fuso; preserve o horário informado.
export function serviceDate(value: string | null, empty: string): string {
  if (!value) return empty;
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(value);
  return match ? `${match[3]}/${match[2]}/${match[1]} às ${match[4]}:${match[5]}` : 'Data indisponível';
}

export async function executeService(service: Service, action: ServiceAction, expected: Session, date = ''): Promise<Service> {
  const current = await getSession();
  if (!current || current.idUsuario !== expected.idUsuario || current.idTecnico !== expected.idTecnico || current.idCliente !== expected.idCliente) {
    throw new Error('A sessão mudou. Saia e entre novamente com a conta deste atendimento.');
  }
  const allowed = action === 'cancelar' ? canCancel(service, current) : action === 'agendar' ? availableAction(service, current) === 'iniciar' : availableAction(service, current) === action;
  if (!allowed) throw new Error('Esta ação não está disponível. Atualize seus serviços.');
  const payload = action === 'agendar' ? schedulePayload(date) : undefined;
  const { data } = await postProtected<Service>(`/servicos/${service.idServico}/${action}`, payload);
  return data;
}
