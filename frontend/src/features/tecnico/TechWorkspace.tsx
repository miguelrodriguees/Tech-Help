import ServiceCard, { type Atendimento } from '../atendimento/ServiceCard';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { isAxiosError } from 'axios';
import { api } from '../../services/api';
import { csrfHeaders } from '../auth/session';

type Pedido = { idSolicitacao:number; titulo:string; descricao:string; tipoAtendimento:string; urgencia:string };
type Proposta = { idProposta:number; idSolicitacao:number; valor:number; mensagem:string; status:string };
const dinheiro = new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'});

export default function TechWorkspace({onHome,onLogin}:{onHome:()=>void;onLogin:()=>void}) {
 const [services,setServices]=useState<Atendimento[]>([]);
 const [pedidos,setPedidos]=useState<Pedido[]>([]);
 const [propostas,setPropostas]=useState<Proposta[]>([]);
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const [expired,setExpired]=useState(false);
 const [reload,setReload]=useState(0);
 const [selected,setSelected]=useState<number|null>(null);
 const [busy,setBusy]=useState(false);
 const lock=useRef(false);
 const [sendError,setSendError]=useState('');
 const [success,setSuccess]=useState('');
 useEffect(()=>{
  const controller=new AbortController();
  Promise.all([api.get<Pedido[]>('/tecnico/solicitacoes',{signal:controller.signal}),api.get<Proposta[]>('/propostas/minhas',{signal:controller.signal}),api.get<Atendimento[]>('/atendimentos/tecnico',{signal:controller.signal})])
   .then(([requests,offers,atendimentos])=>{setPedidos(requests.data);setPropostas(offers.data);setServices(atendimentos.data);})
   .catch(err=>{if(controller.signal.aborted)return;setExpired(isAxiosError(err)&&err.response?.status===401);setError(isAxiosError(err)&&err.response?.status===403?'Esta área exige uma conta de técnico.':'Não foi possível carregar sua área. Tente novamente.');})
   .finally(()=>{if(!controller.signal.aborted)setLoading(false);});
  return()=>controller.abort();
 },[reload]);
 async function enviar(event:FormEvent<HTMLFormElement>,idSolicitacao:number) {
  event.preventDefault();if(lock.current)return;
  const form=new FormData(event.currentTarget);
  lock.current=true;setBusy(true);setSendError('');setSuccess('');
  try {
   const {data}=await api.post<Proposta>('/propostas',{
    idSolicitacao,valor:Number(form.get('valor')),mensagem:String(form.get('mensagem')).trim(),
    prazoEstimadoDias:form.get('prazo')?Number(form.get('prazo')):null,dataDisponivel:form.get('data')||null,
   },{headers:await csrfHeaders()});
   setPropostas(current=>[data,...current]);setSelected(null);setSuccess(`Proposta #${data.idProposta} enviada.`);
  }catch(err){
   const unauthorized=isAxiosError(err)&&err.response?.status===401;
   setExpired(unauthorized);
   setSendError(unauthorized?'Sua sessão expirou. Entre novamente.':isAxiosError(err)&&typeof err.response?.data?.erro==='string'?err.response.data.erro:'Não foi possível confirmar o envio. Seus campos foram preservados. Consulte suas propostas antes de tentar novamente.');
  }finally{lock.current=false;setBusy(false);}
 }
 function retry(){setLoading(true);setError('');setExpired(false);setReload(value=>value+1);}
 return <section className="th-wizard th-workspace th-tech" aria-labelledby="tech-title">
  <button className="th-link" onClick={onHome} disabled={busy}>Voltar ao início</button>
  <h1 id="tech-title">Sua área profissional</h1>
  <p>Encontre um pedido que você pode atender e apresente suas condições.</p>
  {loading&&<p role="status">Carregando solicitações e propostas…</p>}
  {error&&<div role="alert"><p>{error}</p><button className="th-button" onClick={retry}>Tentar novamente</button></div>}
  <fieldset disabled={busy||loading||Boolean(error)} style={{border:0,padding:0,minWidth:0}}>
   {success&&<p role="status">{success}</p>}
   <h2>Meus atendimentos</h2>
   {!services.length?<p>Você ainda não tem atendimentos contratados.</p>:<div className="th-tech-list">{services.map(service=><ServiceCard key={service.idServico} service={service} role="tecnico" onLogin={onLogin} onChange={updated=>setServices(current=>current.map(s=>s.idServico===updated.idServico?updated:s))}/>)}</div>}
   <h2>Solicitações disponíveis</h2>
   {!pedidos.length&&<p>Ainda não há solicitações disponíveis para você.</p>}
   <div className="th-tech-list">{pedidos.map(pedido=><article className="th-panel th-tech-card" key={pedido.idSolicitacao}>
    <h3>{pedido.titulo}</h3>
    <p>{pedido.tipoAtendimento==='REMOTO'?'Atendimento remoto':pedido.tipoAtendimento==='HIBRIDO'?'Atendimento híbrido':'Atendimento presencial'} · {pedido.urgencia==='ALTA'?'Urgente':pedido.urgencia==='BAIXA'?'Sem urgência':'Próximos dias'}</p>
    <details><summary>Ver descrição do pedido</summary><p className="th-request-description">{pedido.descricao}</p></details>
    {propostas.some(p=>p.idSolicitacao===pedido.idSolicitacao)?<p>Você já enviou uma proposta para este pedido.</p>:selected!==pedido.idSolicitacao?<button className="th-button" disabled={busy} onClick={()=>{setSelected(pedido.idSolicitacao);setSendError('');}}>Preparar proposta</button>:<form onSubmit={event=>enviar(event,pedido.idSolicitacao)}>
     <fieldset disabled={busy}>
      <legend>Sua proposta</legend>
      <label className="th-label">Valor do serviço (R$)<input className="th-field" name="valor" type="number" inputMode="decimal" min="0.01" max="9999999999.99" step="0.01" required/></label>
      <label className="th-label">Explique o que está incluído<textarea className="th-field" name="mensagem" maxLength={3000}/></label>
      <label className="th-label">Prazo estimado em dias (opcional)<input className="th-field" name="prazo" type="number" min="0" max="32767" step="1"/></label>
      <label className="th-label">Data disponível (opcional)<input className="th-field" name="data" type="date"/></label>
      <p>A data é uma sugestão para combinar com o cliente. Enviar a proposta não confirma o atendimento.</p>
      {sendError&&<p role="alert">{sendError}</p>}
      <button className="th-button" type="submit">{busy?'Enviando…':'Enviar proposta'}</button>
      <button className="th-link" type="button" onClick={()=>setSelected(null)}>Cancelar</button>
     </fieldset>
    </form>}
   </article>)}</div>
   <h2>Minhas propostas</h2>
   {!propostas.length?<p>Você ainda não enviou propostas.</p>:<ul className="th-tech-list">{propostas.map(p=><li className="th-panel th-tech-card" key={p.idProposta}><h3>Pedido #{p.idSolicitacao}</h3><p>{dinheiro.format(p.valor)} · {({ENVIADA:'Enviada',ACEITA:'Aceita',RECUSADA:'Recusada',CANCELADA:'Cancelada'} as Record<string,string>)[p.status]??p.status}</p><p className="th-request-description">{p.mensagem}</p></li>)}</ul>}
   <button className="th-link" disabled={busy} onClick={retry}>Atualizar solicitações e propostas</button>
  </fieldset>
  {expired&&<button className="th-button" onClick={onLogin}>Entrar novamente</button>}
 </section>;
}
