import { useEffect, useRef, useState, type FormEvent } from 'react';
import { isAxiosError } from 'axios';
import { api } from '../../services/api';
import { csrfHeaders } from '../auth/session';
export type Atendimento={idServico:number;idProposta:number;status:string;dataAgendada:string|null;dataInicio:string|null;dataConclusao:string|null};
type Avaliacao={idAvaliacao:number;nota:number;comentario:string|null};
export default function ServiceCard({service,role,onChange,onLogin}:{service:Atendimento;role:'cliente'|'tecnico';onChange:(s:Atendimento)=>void;onLogin:()=>void}){
 const [busy,setBusy]=useState(false);
 const lock=useRef(false);
 const [error,setError]=useState('');
 const [expired,setExpired]=useState(false);
 const [confirm,setConfirm]=useState(false);
 const [reviews,setReviews]=useState<Avaliacao[]|null>(null);
 const [reload,setReload]=useState(0);
 const [reviewError,setReviewError]=useState(false);
 useEffect(()=>{
  if(service.status!=='CONCLUIDO')return;
  const c=new AbortController();
  api.get<Avaliacao[]>(`/atendimentos/${service.idServico}/avaliacoes/${role}`,{signal:c.signal})
   .then(r=>setReviews(r.data)).catch(err=>{if(!c.signal.aborted){setReviewError(true);setExpired(isAxiosError(err)&&err.response?.status===401);}});
  return()=>c.abort();
 },[service.idServico,service.status,role,reload]);
 async function send(action:string,payload:unknown={}){
  if(lock.current)return;lock.current=true;setBusy(true);setError('');setExpired(false);
  try{
   const {data}=await api.post(`/atendimentos/${service.idServico}/${action}`,payload,{headers:await csrfHeaders()});
   if(action==='avaliacao')setReviews([data as Avaliacao]);else onChange(data as Atendimento);
   setConfirm(false);
  }catch(err){setExpired(isAxiosError(err)&&err.response?.status===401);setError(isAxiosError(err)&&typeof err.response?.data?.erro==='string'?err.response.data.erro:'Não foi possível confirmar a operação. Atualize o atendimento antes de tentar novamente.');}
  finally{lock.current=false;setBusy(false);}
 }
 function review(e:FormEvent<HTMLFormElement>){e.preventDefault();const f=new FormData(e.currentTarget);void send('avaliacao',{nota:Number(f.get('nota')),comentario:String(f.get('comentario')).trim()});}
 const action=role==='tecnico'&&service.status==='AGENDADO'?'iniciar':role==='cliente'&&service.status==='EM_ANDAMENTO'?'concluir':null;
 return <article className="th-panel th-tech-card">
  <h3>Atendimento #{service.idServico}</h3>
  <p>{({AGENDADO:'Aguardando atendimento',EM_ANDAMENTO:'Em andamento',CONCLUIDO:'Concluído'} as Record<string,string>)[service.status]??service.status}</p>
  {service.status==='AGENDADO'&&<p>{service.dataAgendada?`Data combinada: ${new Date(service.dataAgendada).toLocaleString('pt-BR')}`:'Data e horário ainda precisam ser combinados entre vocês.'}</p>}
  {service.dataInicio&&<p>Início: {new Date(service.dataInicio).toLocaleString('pt-BR')}</p>}
  {service.dataConclusao&&<p>Conclusão: {new Date(service.dataConclusao).toLocaleString('pt-BR')}</p>}
  {role==='tecnico'&&service.status==='EM_ANDAMENTO'&&<p>Depois de realizar o serviço, peça ao cliente que confirme a conclusão na conta dele.</p>}
  {action&&(confirm?<div><p>{action==='iniciar'?'Você está começando este atendimento?':'O serviço foi realizado? Confirme apenas se o atendimento terminou.'}</p><button className="th-button" disabled={busy} onClick={()=>send(action)}>{busy?'Salvando…':'Confirmar'}</button><button className="th-link" disabled={busy} onClick={()=>setConfirm(false)}>Voltar</button></div>:<button className="th-button" onClick={()=>setConfirm(true)}>{action==='iniciar'?'Iniciar atendimento':'Confirmar conclusão'}</button>)}
  {error&&<p role="alert">{error}</p>}
  {expired&&<button className="th-link" onClick={onLogin}>Entrar novamente</button>}
  {service.status==='CONCLUIDO'&&(reviewError?<div role="alert"><p>Não foi possível consultar a avaliação.</p><button className="th-link" onClick={()=>{setReviewError(false);setReload(n=>n+1);}}>Consultar novamente</button></div>:reviews===null?<p role="status">Consultando avaliação…</p>:reviews.length?<div><h4>Avaliação do cliente</h4>{reviews.map(r=><div key={r.idAvaliacao}><p>Nota: {r.nota} de 5</p><p className="th-request-description">{r.comentario}</p></div>)}</div>:role==='tecnico'?<p>O cliente ainda não avaliou este serviço.</p>:<form onSubmit={review}><fieldset disabled={busy}><legend>Como foi o atendimento?</legend><label className="th-label">Nota<select className="th-field" name="nota" required defaultValue=""><option value="" disabled>Selecione de 1 a 5</option>{[1,2,3,4,5].map(n=><option value={n} key={n}>{n} de 5</option>)}</select></label><label className="th-label">Comentário (opcional)<textarea className="th-field" name="comentario" maxLength={3000}/></label><button className="th-button" type="submit">{busy?'Enviando…':'Enviar avaliação'}</button></fieldset></form>)}
 </article>;
}
