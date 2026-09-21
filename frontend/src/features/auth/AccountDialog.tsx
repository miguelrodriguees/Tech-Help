import { useEffect, useRef, useState, type FormEvent } from 'react';
import { isAxiosError } from 'axios';
import { api } from '../../services/api';
import { csrfHeaders, entrar, type Conta } from './session';

export default function AccountDialog({onClose,onSuccess,professional=false}: {professional?:boolean;onClose:()=>void;onSuccess:(conta:Conta)=>void}) {
 const dialog=useRef<HTMLDialogElement>(null);
 const [register,setRegister]=useState(professional);
 const [role,setRole]=useState(professional?'tecnico':'cliente');
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState('');
 const [created,setCreated]=useState(false);
 useEffect(()=>{dialog.current?.showModal();},[]);
 async function submit(event:FormEvent<HTMLFormElement>) {
  event.preventDefault(); if(busy)return;
  const data=new FormData(event.currentTarget);
  const email=String(data.get('email')).trim().toLowerCase();
  const senha=String(data.get('senha'));
  if(new TextEncoder().encode(senha).length>72){setError('Use uma senha de até 72 bytes (acentos e emojis ocupam mais espaço).');return;}
  setBusy(true);setError('');
  try {
   if(register) {
    if(senha!==data.get('confirmacao')){setError('As senhas precisam ser iguais.');return;}
    await api.post(`/cadastro/${role}`,{nome:String(data.get('nome')).trim(),email,senha,cpf:String(data.get('cpf')).replace(/\D/g,''),telefone:String(data.get('telefone')).trim(),...(role==='tecnico'?{anosExperiencia:Number(data.get('experiencia')),descricao:String(data.get('descricao')).trim()}: {})},{headers:await csrfHeaders()});
    setCreated(true);setRegister(false);
   }
   const conta=await entrar(email,senha);
   onSuccess(conta);
  } catch(err) {
   const status=isAxiosError(err)?err.response?.status:undefined;
   setError(status===401?'E-mail ou senha incorretos, ou conta inativa.':status===403?'Sua sessão de segurança expirou. Tente novamente.':isAxiosError(err)&&typeof err.response?.data?.erro==='string'?err.response.data.erro:status===400?'Confira os campos e tente novamente.':'Não foi possível conectar. Tente novamente em instantes.');
  } finally {setBusy(false);}
 }
 return <dialog className="th-dialog th-account" ref={dialog} onCancel={e=>{if(busy)e.preventDefault();}} onClose={onClose} aria-labelledby="account-title">
 <button className="th-link" type="button" disabled={busy} onClick={onClose}>Fechar</button>
 <h2 id="account-title">{register?'Crie sua conta':'Entre no TechHelp'}</h2>
 <p>Seu pedido continua guardado enquanto você entra.</p>
 {created&&<p role="status">Conta criada. Se necessário, entre com seu e-mail e senha.</p>}
 <form onSubmit={submit}>
 <fieldset disabled={busy}>
 {register&&<label className="th-label">Tipo de conta<select className="th-field" value={role} onChange={event=>setRole(event.target.value)}><option value="cliente">Cliente</option><option value="tecnico">Profissional de TI</option></select></label>}
 {register&&role==='tecnico'&&<><label className="th-label">Anos de experiência<input className="th-field" name="experiencia" type="number" min="0" max="32767" step="1" required/></label><label className="th-label">Apresentação profissional (opcional)<textarea className="th-field" name="descricao" maxLength={3000}/></label></>}
 {register&&<label className="th-label">Nome<input className="th-field" name="nome" autoComplete="name" required maxLength={120}/></label>}
 <label className="th-label">E-mail<input className="th-field" name="email" type="email" autoComplete="username" required maxLength={254}/></label>
 <label className="th-label">Senha<input className="th-field" name="senha" type="password" autoComplete={register?'new-password':'current-password'} required minLength={register?6:1} maxLength={72}/></label>
 {register&&<><label className="th-label">Confirme a senha<input className="th-field" name="confirmacao" type="password" autoComplete="new-password" required/></label><label className="th-label">CPF<input className="th-field" name="cpf" inputMode="numeric" autoComplete="off" required pattern="[0-9]{11}" maxLength={11} placeholder="11 números, sem pontuação"/></label><label className="th-label">Telefone (opcional)<input className="th-field" name="telefone" type="tel" autoComplete="tel" maxLength={20}/></label></>}
 {error&&<p role="alert">{error}</p>}
 <button className="th-button" type="submit">{busy?'Aguarde…':register?'Criar conta e entrar':'Entrar'}</button>
 <button className="th-link" type="button" onClick={()=>{setRegister(!register);setError('');}}>{register?'Já tenho conta':'Criar conta'}</button>
 </fieldset></form></dialog>;
}
