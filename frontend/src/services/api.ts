import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env?.VITE_API_URL ?? "http://localhost:8080",
  withCredentials: true,
  timeout: 8000,
  headers: {
    "Content-Type": "application/json",
  },
});

export interface Session {
  idUsuario: number;
  idCliente: number | null;
  idTecnico: number | null;
  nome: string;
  email: string;
  perfis: string[];
}

export interface ServiceRequest {
  idSolicitacao: number;
  titulo: string;
  descricao: string;
  status: string;
}

// Um token novo por operação também atende login/logout e sessões de outras abas.
// Nunca repetir automaticamente uma escrita: ela pode já ter sido recebida.
export async function postProtected<T>(path: string, body?: unknown) {
  const { data: csrf } = await api.get<{ headerName: string; token: string }>('/auth/csrf');
  return api.post<T>(path, body, { headers: {
    [csrf.headerName]: csrf.token,
    'Content-Type': body instanceof URLSearchParams ? 'application/x-www-form-urlencoded' : 'application/json',
  } });
}

export async function getSession(): Promise<Session | null> {
  try { return (await api.get<Session>('/auth/me')).data; }
  catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) return null;
    throw error;
  }
}

export function statusOf(error: unknown) {
  return axios.isAxiosError(error) ? error.response?.status : undefined;
}

export function errorMessage(error: unknown): string {
  const status = statusOf(error);
  if (status === 401) return 'E-mail ou senha incorretos, ou sessão encerrada. Entre novamente.';
  if (status === 403) return 'Sua conta não tem permissão ou a sessão mudou. Entre novamente e tente outra vez.';
  if (status === 400) {
    const message = axios.isAxiosError(error) ? error.response?.data?.erro : undefined;
    return typeof message === 'string' ? message : 'Confira os campos informados e tente novamente.';
  }
  return 'Não foi possível confirmar a operação. Verifique sua conexão e a disponibilidade do serviço.';
}
