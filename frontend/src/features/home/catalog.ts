import type { ElementType } from 'react';
import { AppWindow, Laptop, Network, Printer, Server, Shield } from 'lucide-react';
import type { Categoria } from '../../types/Categoria.ts';

export type CatalogState = {
  status: 'loading' | 'ready' | 'error';
  categories: Categoria[];
};

/** Ícone por nome de categoria. Se vier uma categoria nova da API, cai no padrão. */
export function categoryIcon(name: string): ElementType {
  const value = name.toLowerCase();
  if (value.includes('hardware')) return Laptop;
  if (value.includes('rede')) return Network;
  if (value.includes('software')) return AppWindow;
  if (value.includes('segur')) return Shield;
  if (value.includes('impress') || value.includes('perif')) return Printer;
  return Server;
}

/**
 * A API é a única fonte das áreas. Se o formato vier diferente do contrato,
 * preferimos mostrar erro a exibir algo inventado.
 */
export function isCategoria(value: unknown): value is Categoria {
  const item = value as Categoria | null;
  return !!item
    && Number.isSafeInteger(item.idCategoria)
    && item.idCategoria > 0
    && typeof item.nome === 'string'
    && item.nome.trim().length > 0
    && typeof item.ativo === 'boolean';
}

export function parseCatalog(data: unknown): Categoria[] {
  if (!Array.isArray(data) || !data.every(isCategoria)) throw new Error('Catálogo fora do contrato');
  return data.filter(category => category.ativo);
}
