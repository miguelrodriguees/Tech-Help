import {useEffect,useRef,useState,type FormEvent} from 'react';
import {api} from '../../services/api';
import type {Conta} from '../auth/session';
import RentalHistory from './RentalHistory';
import {errorMessage,money,post,type CatalogItem,type Rental} from './api';

export default function RentalWorkspace({conta,onHome,onLogin}:{conta:Conta|null;onHome:()=>void;onLogin:()=>void}){
 const [catalog,setCatalog]=useState<CatalogItem[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState(''),[version,setVersion]=useState(0);
 const [selected,setSelected]=useState<Record<string,number>>({}),[receipt,setReceipt]=useState('RETIRADA'),[back,setBack]=useState('NO_TECHHELP');
 const [start,setStart]=useState(''),[end,setEnd]=useState(''),[address,setAddress]=useState(''),[busy,setBusy]=useState(false),[result,setResult]=useState<Rental|null>(null);
 const [history,setHistory]=useState(0),[sendError,setSendError]=useState('');const lock=useRef(false),intent=useRef<{json:string;key:string}|null>(null);
 useEffect(()=>{const c=new AbortController();api.get<CatalogItem[]>('/locacao/catalogo',{signal:c.signal}).then(r=>setCatalog(r.data)).catch(e=>{if(!c.signal.aborted)setError(errorMessage(e));}).finally(()=>{if(!c.signal.aborted)setLoading(false);});return()=>c.abort();},[version]);
 const choices=catalog.filter(i=>(selected[i.tipo+i.id]??0)>0);
 const days=start&&end?Math.ceil((new Date(end).getTime()-new Date(start).getTime())/86400000):0;
 const rental=choices.reduce((n,i)=>n+i.diaria*selected[i.tipo+i.id],0)*Math.max(0,days);
 const transport=receipt==='ENTREGA'||back==='COLETA';
 async function submit(e:FormEvent){
  e.preventDefault();setSendError('');if(lock.current)return;
  if(!choices.length){setSendError('Escolha pelo menos uma ferramenta ou kit.');return;}
  if(days<1||days>30||new Date(start).getTime()<=Date.now()){setSendError('Escolha uma retirada futura e devolução posterior, em até 30 dias.');return;}
  if(!conta){onLogin();return;}
  const data={retirada:start,devolucao:end,recebimento:receipt,retorno:back,endereco:transport?address:null,itens:choices.map(i=>({tipo:i.tipo,id:i.id,quantidade:selected[i.tipo+i.id]}))};
  const json=JSON.stringify({user:conta.idUsuario,...data});if(intent.current?.json!==json)intent.current={json,key:crypto.randomUUID()};
  lock.current=true;setBusy(true);
  try{const saved=await post<Rental>('/pedidos',{...data,chave:intent.current!.key});setResult(saved);setHistory(n=>n+1);setVersion(n=>n+1);setSelected({});intent.current=null;}
  catch(err){setSendError(errorMessage(err));}finally{lock.current=false;setBusy(false);}
 }
 return <section className="th-wizard th-workspace" aria-labelledby="rental-title">
  <button className="th-link" onClick={onHome}>Voltar à Home</button><h1 id="rental-title">Ferramentas para o seu próximo trabalho.</h1>
  <p>Alugue equipamentos do TechHelp. Retire no local ou solicite entrega e coleta com orçamento conforme o endereço.</p>
  <button className="th-link" disabled={busy||loading} onClick={()=>{setLoading(true);setError('');setVersion(n=>n+1);}}>Atualizar catálogo</button>
  {loading&&<p role="status">Carregando o catálogo…</p>}
  {error&&<div role="alert"><p>{error}</p><button className="th-link" onClick={()=>{setLoading(true);setError('');setVersion(n=>n+1);}}>Tentar novamente</button></div>}
  {!loading&&!error&&!catalog.length&&<div className="th-panel th-tech-card"><h2>O catálogo ainda está vazio</h2><p>Os equipamentos aparecerão aqui quando forem cadastrados pelo TechHelp.</p></div>}
  {result&&<div className="th-panel th-tech-card" role="status"><h2>{result.status==='RESERVADO'?'Reserva confirmada':'Pedido de orçamento enviado'}</h2><p>Aluguel #{result.id}. {result.status==='RESERVADO'?'Combine os detalhes da retirada com o TechHelp.':'Aguarde o cálculo do transporte em Meus aluguéis. Os equipamentos ainda não estão reservados.'}</p></div>}
  {catalog.length>0&&<form onSubmit={submit}><fieldset className="th-rental-form" disabled={busy||loading||!!error}>
   <legend className="th-sr-only">Escolha os equipamentos e o período</legend>
   <div className="th-rental-grid">{catalog.map(item=><article className="th-panel th-tech-card" key={item.tipo+item.id}><span className="th-rental-status">{item.tipo==='KIT'?'Kit técnico':'Ferramenta'}</span><h2>{item.nome}</h2><p>{item.descricao}</p>{item.componentes.length>0&&<ul>{item.componentes.map(c=><li key={c}>{c}</li>)}</ul>}<p><strong>{money(item.diaria)}</strong> / diária</p><p>{item.disponivel>0?`${item.disponivel} unidade(s) possíveis com o estoque atual`:'Indisponível no momento'}</p><label className="th-label">Quantidade de {item.nome}<input className="th-field" type="number" min="0" max={Math.min(20,item.disponivel)} disabled={!item.disponivel} value={selected[item.tipo+item.id]??0} onChange={e=>setSelected(current=>({...current,[item.tipo+item.id]:Number(e.target.value)}))}/></label></article>)}</div>
   <p className="th-footnote">Kits e ferramentas avulsas compartilham estoque. A disponibilidade final é conferida na confirmação; não é uma agenda de reservas por data.</p>
   <div className="th-panel th-tech-card"><h2>Período e transporte</h2><div className="th-rental-grid">
    <label className="th-label">Retirada ou entrega prevista<input className="th-field" type="datetime-local" value={start} onChange={e=>setStart(e.target.value)} required/></label>
    <label className="th-label">Devolução prevista<input className="th-field" type="datetime-local" value={end} onChange={e=>setEnd(e.target.value)} required/></label>
    <label className="th-label">Como deseja receber?<select className="th-field" value={receipt} onChange={e=>setReceipt(e.target.value)}><option value="RETIRADA">Retirar no TechHelp</option><option value="ENTREGA">Receber no endereço (taxa a calcular)</option></select></label>
    <label className="th-label">Como deseja devolver?<select className="th-field" value={back} onChange={e=>setBack(e.target.value)}><option value="NO_TECHHELP">Levar ao TechHelp</option><option value="COLETA">Solicitar coleta (taxa a calcular)</option></select></label>
   </div>
   {transport&&<label className="th-label">Endereço para entrega e/ou coleta<textarea className="th-field" minLength={15} maxLength={600} required value={address} onChange={e=>setAddress(e.target.value)} placeholder="Rua, número, complemento, bairro, cidade/UF e CEP"/><span>Se escolher entrega e coleta, ambas usarão este endereço.</span></label>}
   <p>Diária de 24 horas; frações contam como outra diária. Período máximo: 30 dias. Horários sujeitos à combinação com o TechHelp.</p>
   {choices.length>0&&days>0&&days<=30&&<p><strong>Aluguel estimado: {money(rental)}</strong> por {days} diária(s). {transport?'Transporte a calcular. O total ainda não está definido.':'Sem transporte solicitado.'}</p>}
   <p>{transport?'Você receberá as taxas antes de aceitar o total. Nenhum equipamento será reservado até seu aceite.':'Ao confirmar, os equipamentos serão reservados se houver estoque.'} Não há pagamento online.</p>
   <button className="th-button" type="submit" disabled={!choices.length}>{busy?'Enviando…':transport?'Solicitar orçamento de transporte':'Confirmar reserva'}</button>
   {!conta&&<p>Você pode escolher primeiro. Ao enviar, pediremos que entre na sua conta; suas escolhas serão preservadas.</p>}
   </div>
  </fieldset></form>}
  {sendError&&<div role="alert"><p>{sendError}</p><button className="th-link" onClick={onLogin}>Entrar novamente</button></div>}
  {conta&&<RentalHistory key={`${conta.idUsuario}-${history}`} onLogin={onLogin}/>}
 </section>;
}
