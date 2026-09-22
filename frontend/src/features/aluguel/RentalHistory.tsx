import {useEffect,useRef,useState,type FormEvent} from 'react';
import {api} from '../../services/api';
import {date,errorMessage,money,post,statuses,type Rental} from './api';

export default function RentalHistory({admin=false,onLogin}:{admin?:boolean;onLogin:()=>void}){
 const [items,setItems]=useState<Rental[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const [version,setVersion]=useState(0),[busy,setBusy]=useState(false),[message,setMessage]=useState('');
 const [confirmation,setConfirmation]=useState<{id:number;action:string}|null>(null);
 const lock=useRef(false);
 function refresh(){setError('');setLoading(true);setVersion(n=>n+1);}
 useEffect(()=>{const c=new AbortController();api.get<Rental[]>('/locacao'+(admin?'/admin/pedidos':'/meus'),{signal:c.signal}).then(r=>setItems(r.data)).catch(e=>{if(!c.signal.aborted)setError(errorMessage(e));}).finally(()=>{if(!c.signal.aborted)setLoading(false);});return()=>c.abort();},[version,admin]);
 async function run(id:number,action:string,data:unknown={}){
  if(lock.current)return;lock.current=true;setBusy(true);setError('');setMessage('');
  try{const rental=await post<Rental>(`${admin?'/admin':''}/pedidos/${id}/${action}`,data);setItems(current=>current.map(r=>r.id===id?rental:r));setConfirmation(null);setMessage(`Aluguel #${id} atualizado.`);}
  catch(e){setError(errorMessage(e));}finally{lock.current=false;setBusy(false);}
 }
 function quote(e:FormEvent<HTMLFormElement>,id:number){e.preventDefault();const f=new FormData(e.currentTarget);void run(id,'taxas',{entrega:Number(f.get('entrega')||0),coleta:Number(f.get('coleta')||0),observacao:String(f.get('observacao')).trim()});}
 const descriptions:Record<string,string>={aceitar:'Confirmar o total exibido e reservar os equipamentos? A disponibilidade será conferida agora.',cancelar:'Cancelar este pedido? Se houver estoque reservado, ele será liberado.',entregar:'Você já entregou os equipamentos ao usuário, no balcão ou no endereço combinado?',devolver:'Os equipamentos já retornaram ao TechHelp e foram conferidos? Ao confirmar, o estoque será liberado.'};
 return <section aria-label={admin?'Operação de aluguéis':'Meus aluguéis'}>
  <div className="th-rental-heading"><h2>{admin?'Operação de aluguéis':'Meus aluguéis'}</h2><button className="th-link" disabled={busy||loading} onClick={refresh}>Atualizar</button></div>
  <p>Últimos 100 pedidos. {admin?'Registre apenas operações realmente realizadas.':'As taxas dependem do endereço e serão informadas antes do seu aceite.'}</p>
  {loading&&<p role="status">Consultando aluguéis…</p>}
  {error&&<div role="alert"><p>{error}</p><button className="th-link" onClick={refresh}>Consultar novamente</button><button className="th-link" onClick={onLogin}>Entrar novamente</button></div>}
  {message&&<p role="status">{message}</p>}
  {!loading&&!error&&!items.length&&<p>Nenhum aluguel encontrado.</p>}
  <div className="th-tech-list">{items.map(r=><article className="th-panel th-tech-card" key={r.id}>
   <p className="th-rental-status">Aluguel #{r.id} · {statuses[r.status]??r.status}</p>
   {admin&&<h3>{r.usuario}</h3>}
   <ul className="th-rental-items">{r.itens.map((item,i)=><li key={i}>{item.quantidade} × {item.nome} <span>{money(item.diaria)} / diária por unidade</span></li>)}</ul>
   <dl className="th-rental-summary"><div><dt>Período solicitado</dt><dd>{date(r.retirada)} até {date(r.devolucao)}</dd></div><div><dt>Recebimento</dt><dd>{r.recebimento==='ENTREGA'?'Entrega no endereço':'Retirada no TechHelp'}</dd></div><div><dt>Devolução</dt><dd>{r.retorno==='COLETA'?'Coleta no endereço':'Devolução no TechHelp'}</dd></div>{r.endereco&&<div><dt>Endereço de transporte</dt><dd>{r.endereco}</dd></div>}</dl>
   <dl className="th-rental-price"><div><dt>Aluguel</dt><dd>{money(r.aluguel)}</dd></div><div><dt>Entrega</dt><dd>{r.recebimento==='ENTREGA'?money(r.entrega):'Não solicitada'}</dd></div><div><dt>Coleta</dt><dd>{r.retorno==='COLETA'?money(r.coleta):'Não solicitada'}</dd></div><div><dt>Total</dt><dd>{r.total===null?'Aguardando cálculo':money(r.total)}</dd></div></dl>
   {r.observacao&&<p><strong>Transporte:</strong> {r.observacao}</p>}
   {r.status==='AGUARDANDO_TAXA'&&<p>Os equipamentos ainda não estão reservados. O total será apresentado antes da confirmação.</p>}
   {r.status==='AGUARDANDO_ACEITE'&&<p>Confira as taxas. A reserva depende de disponibilidade no momento do aceite.</p>}
   {admin&&r.status==='AGUARDANDO_TAXA'&&<form onSubmit={e=>quote(e,r.id)}><fieldset className="th-rental-form" disabled={busy||loading}><legend>Orçamento de transporte</legend>
    {r.recebimento==='ENTREGA'&&<label className="th-label">Taxa de entrega (R$)<input className="th-field" type="number" name="entrega" min="0" max="9999999999.99" step="0.01" required/></label>}
    {r.retorno==='COLETA'&&<label className="th-label">Taxa de coleta (R$)<input className="th-field" type="number" name="coleta" min="0" max="9999999999.99" step="0.01" required/></label>}
    <label className="th-label">Condições e referência de distância<textarea className="th-field" name="observacao" maxLength={1000} required placeholder="Informe distância considerada, condições e combinação de horários."/></label><button className="th-button">Enviar orçamento ao usuário</button>
   </fieldset></form>}
   <div className="th-workspace-actions">
    {!admin&&r.status==='AGUARDANDO_ACEITE'&&<button className="th-button" disabled={busy||loading} onClick={()=>setConfirmation({id:r.id,action:'aceitar'})}>Aceitar total de {money(r.total)}</button>}
    {admin&&r.status==='RESERVADO'&&<button className="th-button" disabled={busy||loading} onClick={()=>setConfirmation({id:r.id,action:'entregar'})}>Registrar {r.recebimento==='ENTREGA'?'entrega':'retirada'}</button>}
    {admin&&['RETIRADO','ATRASADO'].includes(r.status)&&<button className="th-button" disabled={busy||loading} onClick={()=>setConfirmation({id:r.id,action:'devolver'})}>Confirmar devolução recebida</button>}
    {['AGUARDANDO_TAXA','AGUARDANDO_ACEITE','RESERVADO'].includes(r.status)&&<button className="th-link" disabled={busy||loading} onClick={()=>setConfirmation({id:r.id,action:'cancelar'})}>Cancelar pedido</button>}
   </div>
   {confirmation?.id===r.id&&<div className="th-confirmation"><p>{descriptions[confirmation.action]}</p><p>Nenhuma cobrança online será realizada.</p><button className="th-button" disabled={busy||loading} onClick={()=>run(r.id,confirmation.action)}>{busy?'Salvando…':'Confirmar'}</button><button className="th-link" disabled={busy} onClick={()=>setConfirmation(null)}>Voltar</button></div>}
  </article>)}</div>
 </section>;
}
