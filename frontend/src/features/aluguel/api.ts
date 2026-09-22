import { api } from '../../services/api';
import { csrfHeaders } from '../auth/session';
import { isAxiosError } from 'axios';
export interface CatalogItem {id:number;tipo:'FERRAMENTA'|'KIT';nome:string;descricao:string|null;diaria:number;disponivel:number;componentes:string[]}
export interface Rental {id:number;status:string;usuario:string;recebimento:string;retorno:string;retirada:string;devolucao:string;endereco:string|null;observacao:string|null;aluguel:number;entrega:number|null;coleta:number|null;total:number|null;itens:{nome:string;quantidade:number;diaria:number}[]}
export const money=(n:number|null)=>n===null?'A calcular':new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(n);
export const date=(s:string)=>new Date(s).toLocaleString('pt-BR',{dateStyle:'short',timeStyle:'short'});
export const statuses:Record<string,string>={AGUARDANDO_TAXA:'Aguardando cálculo do transporte',AGUARDANDO_ACEITE:'Aguardando seu aceite',RESERVADO:'Reserva confirmada',RETIRADO:'Com o usuário',DEVOLVIDO:'Devolução recebida',CANCELADO:'Cancelado',ATRASADO:'Devolução atrasada'};
export function errorMessage(err:unknown){return isAxiosError(err)?err.response?.status===401?'Sua sessão expirou. Entre novamente.':err.response?.status===403?'Você não tem permissão para esta operação.':typeof err.response?.data?.erro==='string'?err.response.data.erro:'Não foi possível concluir. Atualize para conferir o resultado antes de tentar novamente.':'Não foi possível concluir a operação.';}
export async function post<T>(path:string,data:unknown={}){return (await api.post<T>('/locacao'+path,data,{headers:await csrfHeaders()})).data;}
