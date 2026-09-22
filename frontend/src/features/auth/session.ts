import { api } from '../../services/api';
export interface Conta { idUsuario: number; idCliente: number | null; idTecnico: number | null; nome: string; email: string; administrador?: boolean }
export async function csrfHeaders() {
 const { data } = await api.get<{token:string;headerName:string}>('/auth/csrf');
 return { [data.headerName]: data.token };
}
export async function entrar(email: string, senha: string) {
 const headers = await csrfHeaders();
 await api.post('/auth/login', new URLSearchParams({email,senha}), {headers:{...headers,'Content-Type':'application/x-www-form-urlencoded'}});
 return (await api.get<Conta>('/auth/me')).data;
}
export async function sair() {
 await api.post('/auth/logout', {}, {headers:await csrfHeaders()});
}
