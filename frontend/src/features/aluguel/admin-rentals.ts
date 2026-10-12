import { api, getSession, postProtected, type Session } from '../../services/api.ts';
import { isAdmin } from '../catalogo/catalog.ts';
import type { Rental } from './rentals.ts';

export interface AdminRental extends Rental { dataRetirada: string | null; dataDevolucao: string | null }
export interface RentalPage { content: { aluguel: AdminRental; nomeUsuario: string }[]; number: number; totalPages: number }
export type RentalAction = 'retirar' | 'devolver';

async function checkAdmin(expected: Session) {
  const current = await getSession();
  if (!isAdmin(expected) || !isAdmin(current) || current?.idUsuario !== expected.idUsuario) {
    throw new Error('A sessão mudou ou não possui acesso administrativo. Entre novamente.');
  }
}
export async function loadAdminRentals(session: Session, page: number, signal: AbortSignal) {
  await checkAdmin(session);
  return (await api.get<RentalPage>('/alugueis/administracao', { params: { pagina: page }, signal })).data;
}
export async function manageRental(session: Session, rental: AdminRental, action: RentalAction) {
  await checkAdmin(session);
  if (!Number.isSafeInteger(rental.idAluguel) || rental.idAluguel <= 0 ||
      (action === 'retirar' ? rental.status !== 'RESERVADO' : action !== 'devolver' || !['RETIRADO', 'ATRASADO'].includes(rental.status))) {
    throw new Error('Operação indisponível para este aluguel. Atualize a lista.');
  }
  return (await postProtected<AdminRental>('/alugueis/' + rental.idAluguel + '/' + action)).data;
}
export function overdue(rental: AdminRental, now = new Date()) {
  // Compara o relógio de Brasília com o LocalDateTime recebido, sem depender do fuso do navegador.
  const parts = new Intl.DateTimeFormat('sv-SE', { timeZone: 'America/Sao_Paulo', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23' }).format(now).replace(' ', 'T');
  return ['RETIRADO', 'ATRASADO'].includes(rental.status) && rental.dataPrevistaDevolucao.slice(0, 19) < parts;
}
