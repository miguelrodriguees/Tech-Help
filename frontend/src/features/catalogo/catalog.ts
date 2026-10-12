import { api, getSession, postProtected, type Session } from '../../services/api.ts';

export interface Kit { idKit: number; nome: string; descricao: string | null; valorDiaria: number; status: string }
export interface Tool { idFerramenta: number; nome: string; descricao: string | null; quantidadeTotal: number; quantidadeDisponivel: number; valorDiaria: number; status: string }
export interface Component { idKit: number; idFerramenta: number; quantidade: number }
export const isAdmin = (session: Session | null) => Boolean(session?.perfis.includes('ADMIN'));

export function catalogPayload(nome: string, descricao: string, diaria: string, quantidade?: string) {
  const name = nome.trim();
  if (!name || name.length > 140) throw new Error('Informe um nome de até 140 caracteres.');
  const price = diaria.trim().replace(',', '.');
  if (!/^\d{1,10}(\.\d{1,2})?$/.test(price)) throw new Error('Informe uma diária válida, com até duas casas decimais.');
  const base = { nome: name, descricao: descricao.trim(), valorDiaria: Number(price) };
  if (quantidade === undefined) return base;
  return { ...base, quantidadeTotal: integerQuantity(quantidade, 0) };
}

export function integerQuantity(value: string, min = 1) {
  if (!/^\d+$/.test(value)) throw new Error('Informe uma quantidade inteira.');
  const quantity = Number(value);
  if (!Number.isSafeInteger(quantity) || quantity < min || quantity > 2147483647) throw new Error('Quantidade fora do limite permitido.');
  return quantity;
}

export function editCatalogPayload(tool: boolean, nome: string, descricao: string, diaria: string, status: string, quantidade?: string) {
  if (!(tool ? ['DISPONIVEL', 'MANUTENCAO', 'INATIVA'] : ['DISPONIVEL', 'INATIVO']).includes(status)) {
    throw new Error('Selecione uma situação válida. Kits reservados não podem ser editados.');
  }
  return { ...catalogPayload(nome, descricao, diaria, tool ? quantidade : undefined), status };
}

type Operation =
  | { kind: 'tool'; body: ReturnType<typeof catalogPayload> }
  | { kind: 'kit'; body: ReturnType<typeof catalogPayload> }
  | { kind: 'editTool'; id: number; body: ReturnType<typeof editCatalogPayload> }
  | { kind: 'editKit'; id: number; body: ReturnType<typeof editCatalogPayload> }
  | { kind: 'component'; kitId: number; toolId: number; quantity: number }
  | { kind: 'remove'; kitId: number; toolId: number };

export async function saveCatalog(expected: Session, operation: Operation): Promise<void> {
  const current = await getSession();
  if (!isAdmin(expected) || !isAdmin(current) || current?.idUsuario !== expected.idUsuario) {
    throw new Error('A sessão mudou ou não possui acesso administrativo. Entre novamente.');
  }
  if (operation.kind === 'tool' || operation.kind === 'kit') {
    await postProtected(operation.kind === 'tool' ? '/ferramentas' : '/kits', operation.body);
    return;
  }
  if (operation.kind === 'editTool' || operation.kind === 'editKit') {
    if (!Number.isSafeInteger(operation.id) || operation.id <= 0) throw new Error('Selecione um registro válido.');
    const { data: csrf } = await api.get<{ headerName: string; token: string }>('/auth/csrf');
    await api.put((operation.kind === 'editTool' ? '/ferramentas/' : '/kits/') + operation.id, operation.body,
      { headers: { [csrf.headerName]: csrf.token } });
    return;
  }
  if (![operation.kitId, operation.toolId].every(id => Number.isSafeInteger(id) && id > 0)) throw new Error('Selecione um kit e uma ferramenta válidos.');
  const path = '/kits/' + operation.kitId + '/ferramentas';
  if (operation.kind === 'component') {
    integerQuantity(String(operation.quantity));
    await postProtected(path, { idFerramenta: operation.toolId, quantidade: operation.quantity });
  } else {
    const { data: csrf } = await api.get<{ headerName: string; token: string }>('/auth/csrf');
    await api.delete(path + '/' + operation.toolId, { headers: { [csrf.headerName]: csrf.token } });
  }
}
