// Execute apenas contra backend conectado a banco de testes: cria registros persistentes.
import assert from 'node:assert/strict';
import { randomUUID, randomInt } from 'node:crypto';
if (!process.argv.includes('--banco-de-testes')) {
 console.error('Use: node scripts/validar-fluxo.mjs --banco-de-testes [http://localhost:8080]');
 process.exit(1);
}
const base=process.argv.find(a=>a.startsWith('http'))??'http://localhost:8080';
const run=randomUUID();
function session(){
 const cookies=new Map();
 async function call(path,{method='GET',body,headers={},expected=200,csrf=true}={}){
  if(method!=='GET'&&csrf){const token=await call('/auth/csrf');headers={...headers,[token.headerName]:token.token};}
  const form=body instanceof URLSearchParams;
  const response=await fetch(base+path,{method,redirect:'manual',signal:AbortSignal.timeout(15000),headers:{...headers,Cookie:[...cookies].map(([k,v])=>`${k}=${v}`).join('; '),...(body?{'Content-Type':form?'application/x-www-form-urlencoded':'application/json'}:{})},body:body?(form?body:JSON.stringify(body)):undefined});
  for(const cookie of response.headers.getSetCookie()){const first=cookie.split(';')[0];const split=first.indexOf('=');cookies.set(first.slice(0,split),first.slice(split+1));}
  const text=await response.text();
  assert.equal(response.status,expected,`${method} ${path}: esperado ${expected}, recebido ${response.status}: ${text.slice(0,300)}`);
  return text?JSON.parse(text):null;
 }
 return {call};
}
async function account(role,index){
 const s=session();const email=`teste-${run}-${index}@example.test`;const senha=`Th!${randomUUID()}`;
 await s.call(`/cadastro/${role}`,{method:'POST',expected:201,body:{nome:`Teste ${role}`,email,senha,cpf:String(randomInt(10000000000,99999999999)),...(role==='tecnico'?{anosExperiencia:1}: {})}});
 await s.call('/auth/login',{method:'POST',body:new URLSearchParams({email,senha}),expected:204});
 return s;
}
try{
 const anon=session();await anon.call('/servicos/meus',{expected:401});
 const cliente=await account('cliente',1),outro=await account('cliente',2),tecnico=await account('tecnico',3),tecnico2=await account('tecnico',4);
 const categorias=await cliente.call('/categorias');assert.ok(categorias.length,'Banco precisa conter categorias e perfis de cadastro');
 const payload={idCategoria:categorias[0].idCategoria,titulo:`Teste integrado ${run}`,descricao:'Pedido automatizado em banco de testes',tipoAtendimento:'REMOTO',urgencia:'NORMAL'};
 const headers={'Idempotency-Key':randomUUID()};
 const pedido=await cliente.call('/solicitacoes',{method:'POST',expected:201,body:payload,headers});
 const repetido=await cliente.call('/solicitacoes',{method:'POST',expected:200,body:payload,headers});assert.equal(repetido.idSolicitacao,pedido.idSolicitacao);
 await cliente.call('/tecnico/solicitacoes',{expected:403});
 const propostas=[];
 for(const t of [tecnico,tecnico2])propostas.push(await t.call('/propostas',{method:'POST',expected:201,body:{idSolicitacao:pedido.idSolicitacao,valor:150,mensagem:'Teste',prazoEstimadoDias:2}}));
 await tecnico.call('/propostas',{method:'POST',expected:400,body:{idSolicitacao:pedido.idSolicitacao,valor:150}});
 await outro.call(`/cliente/solicitacoes/${pedido.idSolicitacao}/propostas`,{expected:404});
 const offers=await cliente.call(`/cliente/solicitacoes/${pedido.idSolicitacao}/propostas`);assert.equal(offers.length,2);
 const accept=`/servicos/aceitar-proposta/${propostas[0].idProposta}`;
 await outro.call(accept,{method:'POST',expected:404});
 const service=await cliente.call(accept,{method:'POST',expected:201});
 assert.equal((await cliente.call(accept,{method:'POST',expected:201})).idServico,service.idServico);
 await cliente.call(`/servicos/aceitar-proposta/${propostas[1].idProposta}`,{method:'POST',expected:400});
 const path=`/atendimentos/${service.idServico}`;
 await cliente.call(`${path}/concluir`,{method:'POST',expected:400});
 await cliente.call(`${path}/avaliacao`,{method:'POST',expected:400,body:{nota:5}});
 await tecnico2.call(`${path}/iniciar`,{method:'POST',expected:404});
 assert.equal((await tecnico.call(`${path}/iniciar`,{method:'POST'})).status,'EM_ANDAMENTO');
 await outro.call(`${path}/concluir`,{method:'POST',expected:404});
 assert.equal((await cliente.call(`${path}/concluir`,{method:'POST'})).status,'CONCLUIDO');
 await cliente.call(`${path}/avaliacao`,{method:'POST',expected:400,body:{nota:6}});
 await cliente.call(`${path}/avaliacao`,{method:'POST',body:{nota:5,comentario:'Teste automatizado'}});
 await cliente.call(`${path}/avaliacao`,{method:'POST',expected:400,body:{nota:5}});
 assert.equal((await tecnico.call(`${path}/avaliacoes/tecnico`))[0].nota,5);
 console.log('PASSOU: cadastro, sessão, solicitação, propostas, contratação, atendimento, avaliação e verificações de acesso.');
 console.log(`Registros de teste mantidos. Execução: ${run}, solicitação: ${pedido.idSolicitacao}`);
}catch(error){console.error(`FALHOU (execução ${run}):`,error.message);process.exitCode=1;}
