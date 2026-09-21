import ServiceCard, { type Atendimento } from '../atendimento/ServiceCard';
import { useEffect, useRef, useState } from 'react';
import { isAxiosError } from 'axios';
import { api } from '../../services/api';
import { csrfHeaders } from '../auth/session';
type Oferta={idProposta:number;nomeProfissional:string;valor:number;mensagem:string;prazoEstimadoDias:number|null;dataDisponivel:string|null;status:string};
const money=new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'});
const statuses:Record<string,string>={ENVIADA:'Enviada',ACEITA:'Aceita',RECUSADA:'Recusada',CANCELADA:'Cancelada',AGENDADO:'Aguardando atendimento',EM_ANDAMENTO:'Em andamento',CONCLUIDO:'Concluído'};
export default function ReceivedOffers({idPedido,onLogin,onStatus}:{idPedido:number;onLogin:()=>void;onStatus:(status:string)=>void}) {
 const [offers,setOffers]=useState<Oferta[]>([]);
 const [services,setServices]=useState<Atendimento[]>([]);
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const [expired,setExpired]=useState(false);
 const [reload,setReload]=useState(0);
 const [confirm,setConfirm]=useState<number|null>(null);
 const [busy,setBusy]=useState(false);
 const [success,setSuccess]=useState('');
 const lock=useRef(false);
 useEffect(()=>{
  const c=new AbortController();
  Promise.all([api.get<Oferta[]>(`/cliente/solicitacoes/${idPedido}/propostas`,{signal:c.signal}),api.get<Atendimento[]>('/servicos/meus',{signal:c.signal})])
   .then(([a,b])=>{setOffers(a.data);setServices(b.data);})
   .catch(err=>{if(c.signal.aborted)return;setExpired(isAxiosError(err)&&err.response?.status===401);setError('Não foi possível consultar as propostas e o atendimento.');})
   .finally(()=>{if(!c.signal.aborted)setLoading(false);});
  return()=>c.abort();
 },[idPedido,reload]);
 function refresh(){setLoading(true);setError('');setExpired(false);setReload(n=>n+1);}
 async function accept(offer:Oferta){
  if(lock.current)return;lock.current=true;setBusy(true);setError('');
  try{
   const {data}=await api.post<Atendimento>(`/servicos/aceitar-proposta/${offer.idProposta}`,{},{headers:await csrfHeaders()});
   setServices(current=>[...current.filter(s=>s.idServico!==data.idServico),data]);
   setOffers(current=>current.map(o=>({...o,status:o.idProposta===offer.idProposta?'ACEITA':o.status==='ENVIADA'?'RECUSADA':o.status})));
   onStatus(data.status==='CONCLUIDO'?'CONCLUIDA':'CONTRATADA');
   setConfirm(null);setSuccess(`Proposta aceita. Atendimento #${data.idServico} criado.`);
  }catch(err){setExpired(isAxiosError(err)&&err.response?.status===401);setError(isAxiosError(err)&&typeof err.response?.data?.erro==='string'?err.response.data.erro:'Não foi possível confirmar a contratação. Atualize para consultar o resultado antes de tentar novamente.');}
  finally{lock.current=false;setBusy(false);}
 }
 const service=services.find(s=>offers.some(o=>o.idProposta===s.idProposta));
 return <div className="th-received-offers">
  <h3>Propostas recebidas</h3>
  {success&&<p role="status">{success}</p>}
  {error&&<p role="alert">{error}</p>}
  {expired&&<button className="th-link" onClick={onLogin}>Entrar novamente</button>}
  {loading&&<p role="status">Carregando…</p>}
  <fieldset disabled={loading||busy} style={{border:0,padding:0,minWidth:0}}>
   {!error&&!offers.length&&<p>Ainda não há propostas para este pedido.</p>}
   {service&&<ServiceCard service={service} role="cliente" onLogin={onLogin} onChange={updated=>{setServices(current=>current.map(s=>s.idServico===updated.idServico?updated:s));if(updated.status==='CONCLUIDO')onStatus('CONCLUIDA');}}/>}
   <ul className="th-tech-list">{offers.map(o=><li className="th-panel th-tech-card" key={o.idProposta}>
    <h4>{o.nomeProfissional}</h4><p><strong>{money.format(o.valor)}</strong> · {statuses[o.status]??o.status}</p>
    <p className="th-request-description">{o.mensagem||'Sem mensagem adicional.'}</p>
    <p>{o.prazoEstimadoDias===null?'Prazo não informado':`Prazo estimado: ${o.prazoEstimadoDias} dia(s)`}</p>
    <p>{o.dataDisponivel?`Disponibilidade sugerida: ${o.dataDisponivel.split('-').reverse().join('/')}`:'Disponibilidade a combinar'}</p>
    {!service&&o.status==='ENVIADA'&&(confirm===o.idProposta?<div><p>Confirmar esta proposta por {money.format(o.valor)}? As outras propostas enviadas serão recusadas. Nenhum pagamento será realizado pelo site.</p><button className="th-button" disabled={busy} onClick={()=>accept(o)}>{busy?'Confirmando…':'Confirmar contratação'}</button><button className="th-link" disabled={busy} onClick={()=>setConfirm(null)}>Voltar</button></div>:<button className="th-button" disabled={busy} onClick={()=>setConfirm(o.idProposta)}>Escolher proposta</button>)}
   </li>)}</ul>
  </fieldset>
  <button className="th-link" disabled={busy||loading} onClick={refresh}>Atualizar propostas e atendimento</button>
 </div>;
}
