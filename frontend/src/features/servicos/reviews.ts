import { getSession, postProtected, type Session } from '../../services/api.ts';
import type { Service } from './services.ts';

export interface Review {
  idAvaliacao: number;
  idServico: number;
  idAvaliador: number;
  idAvaliado: number;
  nota: number;
  comentario: string | null;
}

export function reviewPayload(nota: number, comentario: string) {
  if (!Number.isInteger(nota) || nota < 1 || nota > 5) throw new Error('Escolha uma nota entre 1 e 5.');
  if (comentario.length > 3000) throw new Error('O comentário deve ter no máximo 3000 caracteres.');
  return { nota, comentario: comentario.trim() || null };
}

export async function submitReview(service: Service, expected: Session, nota: number, comentario: string) {
  const payload = reviewPayload(nota, comentario);
  const current = await getSession();
  if (!current || current.idUsuario !== expected.idUsuario || current.idCliente !== service.idCliente || !current.perfis.includes('CLIENTE')) {
    throw new Error('A sessão mudou. Entre novamente com a conta do cliente deste serviço.');
  }
  if (service.status !== 'CONCLUIDO') throw new Error('O atendimento precisa estar concluído para ser avaliado.');
  return (await postProtected<Review>(`/avaliacoes/servico/${service.idServico}/cliente`, payload)).data;
}
