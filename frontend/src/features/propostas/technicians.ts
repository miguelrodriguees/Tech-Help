import { api } from '../../services/api.ts';

export type TechnicianNames = Record<number, string>;
export const technicianLabel = (id: number, names: TechnicianNames) => names[id]?.trim() || `Técnico #${id}`;

export async function loadTechnicianNames(ids: number[], signal?: AbortSignal): Promise<TechnicianNames> {
  const unique = [...new Set(ids.filter(id => Number.isSafeInteger(id) && id > 0))];
  const names: TechnicianNames = {};
  const batches: number[][] = [];
  for (let i = 0; i < unique.length; i += 50) batches.push(unique.slice(i, i + 50));
  await Promise.all(batches.map(async batch => {
    try {
      const { data } = await api.get<{ idTecnico: number; nome: string }[]>('/tecnicos/resumos', { params: { ids: batch.join(',') }, signal });
      for (const item of data) {
        if (batch.includes(item.idTecnico) && typeof item.nome === 'string' && item.nome.trim()) names[item.idTecnico] = item.nome.trim();
      }
    } catch { /* Uma falha na identificação não deve impedir a consulta do atendimento. */ }
  }));
  return names;
}
