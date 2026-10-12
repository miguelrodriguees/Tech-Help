import { api, getSession, postProtected, type Session } from '../../services/api.ts';
import { integerQuantity, type Kit } from '../catalogo/catalog.ts';
import { schedulePayload } from '../servicos/services.ts';

export interface Rental {
  idAluguel: number; idUsuario: number; status: string; valorTotal: number;
  dataPrevistaRetirada: string; dataPrevistaDevolucao: string;
}
export interface RentalItem { idKit: number; quantidade: number; valorDiaria: number }
export const rentalStatus = (value: string) => ({
  RESERVADO: 'Reservado', RETIRADO: 'Retirado', DEVOLVIDO: 'Devolvido', ATRASADO: 'Atrasado', CANCELADO: 'Cancelado',
}[value] ?? value);

export function reservationPayload(session: Session, kitId: number, quantity: string, start: string, end: string) {
  if (!Number.isSafeInteger(kitId) || kitId <= 0) throw new Error('Selecione um kit.');
  const retirada = schedulePayload(start).dataAgendada;
  const devolucao = schedulePayload(end).dataAgendada;
  if (devolucao <= retirada) throw new Error('A devolução precisa ser depois da retirada.');
  return { idUsuario: session.idUsuario, idKit: kitId, quantidade: integerQuantity(quantity),
    dataPrevistaRetirada: retirada, dataPrevistaDevolucao: devolucao };
}

export function estimate(kit: Kit, quantity: number, start: string, end: string) {
  // Mesma regra atual do backend: diferença entre as datas, com mínimo de uma diária.
  const days = Math.max(1, (Date.parse(end.slice(0, 10) + 'T00:00:00Z') - Date.parse(start.slice(0, 10) + 'T00:00:00Z')) / 86400000);
  return { days, total: Math.round(Number(kit.valorDiaria) * 100) * quantity * days / 100 };
}

async function confirmSession(expected: Session) {
  const current = await getSession();
  if (!current || current.idUsuario !== expected.idUsuario) throw new Error('A sessão mudou. Entre novamente com a conta deste aluguel.');
}
export async function reserve(expected: Session, body: ReturnType<typeof reservationPayload>) {
  await confirmSession(expected);
  if (body.idUsuario !== expected.idUsuario) throw new Error('Conta inválida para a reserva.');
  return (await postProtected<Rental>('/alugueis/reservar', body)).data;
}
export async function cancelRental(expected: Session, rental: Rental) {
  await confirmSession(expected);
  if (rental.idUsuario !== expected.idUsuario || rental.status !== 'RESERVADO') throw new Error('Este aluguel não pode ser cancelado por esta conta.');
  return (await postProtected<Rental>('/alugueis/' + rental.idAluguel + '/cancelar')).data;
}
export async function loadRentals(expected: Session, signal: AbortSignal) {
  await confirmSession(expected);
  return (await api.get<Rental[]>('/alugueis/usuario/' + expected.idUsuario, { signal })).data;
}
