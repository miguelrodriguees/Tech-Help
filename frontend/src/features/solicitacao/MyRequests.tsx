import ReceivedOffers from './ReceivedOffers';
import { useEffect, useState } from 'react';
import { isAxiosError } from 'axios';
import { api } from '../../services/api';
interface Pedido {idSolicitacao:number;titulo:string;descricao:string;status:string;tipoAtendimento:string;urgencia:string}
const statuses: Record<string,string>={ABERTA:'Aberta',EM_NEGOCIACAO:'Recebendo propostas',CONTRATADA:'Contratada',EM_ANDAMENTO:'Em andamento',CONCLUIDA:'Concluída',CANCELADA:'Cancelada'};
export default function MyRequests({onHome,onCreate,onLogin}:{onHome:()=>void;onCreate:()=>void;onLogin:()=>void}) {
 const [state,setState]=useState<{status:'loading'|'ready'|'error'|'expired';pedidos:Pedido[]}>({status:'loading',pedidos:[]});
 const [reload,setReload]=useState(0);
 const [open,setOpen]=useState<number|null>(null);
 useEffect(()=>{
  const controller=new AbortController();
  api.get<Pedido[]>('/solicitacoes/minhas',{signal:controller.signal}).then(({data})=>{
   if(!Array.isArray(data))throw new Error('Resposta inválida');
   setState({status:'ready',pedidos:data});
  }).catch(err=>{if(!controller.signal.aborted)setState({status:isAxiosError(err)&&err.response?.status===401?'expired':'error',pedidos:[]});});
  return()=>controller.abort();
 },[reload]);
 return <section className="th-wizard th-my-requests" aria-labelledby="my-requests-title">
  <button type="button" className="th-link" onClick={onHome}>Voltar à Home</button>
  <h1 id="my-requests-title">Minhas solicitações</h1>
  <p>Acompanhe os pedidos salvos na sua conta.</p>
  <button type="button" className="th-button" onClick={onCreate}>Criar solicitação</button>
  {state.status==='loading'&&<p role="status">Carregando seus pedidos…</p>}
  {state.status==='expired'&&<div role="alert"><p>Sua sessão expirou. Entre novamente para consultar seus pedidos.</p><button className="th-button" onClick={onLogin}>Entrar</button></div>}
  {(state.status==='error'||state.status==='expired')&&<div role="alert"><p>Não foi possível consultar os pedidos.</p><button className="th-button secondary" onClick={()=>{setState({status:'loading',pedidos:[]});setReload(n=>n+1);}}>Tentar novamente</button></div>}
  {state.status==='ready'&&state.pedidos.length===0&&<p>Você ainda não publicou solicitações.</p>}
  {state.status==='ready'&&state.pedidos.map(pedido=><article className="th-panel th-request-item" key={pedido.idSolicitacao}><span>Pedido #{pedido.idSolicitacao} · {statuses[pedido.status]??pedido.status}</span><h2>{pedido.titulo}</h2><p>{pedido.tipoAtendimento==='REMOTO'?'Atendimento remoto':pedido.tipoAtendimento==='HIBRIDO'?'Atendimento híbrido':'Atendimento presencial'}</p><details><summary>Ver informações do pedido</summary><p style={{whiteSpace:'pre-wrap'}}>{pedido.descricao}</p></details><button className="th-link" onClick={()=>setOpen(pedido.idSolicitacao)}>Ver propostas e atendimento</button>{open===pedido.idSolicitacao&&<ReceivedOffers idPedido={pedido.idSolicitacao} onLogin={onLogin} onStatus={status=>setState(current=>({...current,pedidos:current.pedidos.map(p=>p.idSolicitacao===pedido.idSolicitacao?{...p,status}:p)}))}/>}</article>)}
 </section>;
}
