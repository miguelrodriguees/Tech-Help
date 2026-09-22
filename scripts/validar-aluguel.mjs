// Execute apenas contra backend conectado a banco de testes: cria registros persistentes.
import assert from 'node:assert/strict';
import { randomUUID, randomInt } from 'node:crypto';
if (!process.argv.includes('--banco-de-testes')) {
 console.error('Use: node scripts/validar-aluguel.mjs --banco-de-testes [http://localhost:8080]');
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
// Use credenciais de ADMIN do banco de TESTES. Nunca há promoção automática de usuário.
if(!process.env.TECHHELP_ADMIN_EMAIL||!process.env.TECHHELP_ADMIN_SENHA){console.error('Defina TECHHELP_ADMIN_EMAIL e TECHHELP_ADMIN_SENHA de uma conta ADMIN do banco de testes.');process.exit(1);}
const verb=(body,expected=200)=>({method:'POST',body,expected});
const testStart=Date.now();
const future=hours=>{const d=new Date(testStart+hours*3600000);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:00`;};
try {
 const admin=session();await admin.call('/auth/login',verb(new URLSearchParams({email:process.env.TECHHELP_ADMIN_EMAIL,senha:process.env.TECHHELP_ADMIN_SENHA}),204));
 assert.equal((await admin.call('/auth/me')).administrador,true,'A conta deve ter perfil ADMIN');
 const a=await account('cliente',1),b=await account('cliente',2),anon=session();
 await anon.call('/locacao/meus',{expected:401});await a.call('/locacao/admin/pedidos',{expected:403});await a.call('/locacao/admin/ferramentas',verb({nome:'Não criar',quantidade:1,diaria:5},403));
 const nome=`TESTE aluguel ${run}`;
 await admin.call('/locacao/admin/ferramentas',verb({nome,descricao:'Equipamento exclusivo do teste automatizado',quantidade:2,diaria:10},201));
 const catalog=()=>anon.call('/locacao/catalogo');
 const tool=(await catalog()).find(i=>i.nome===nome);assert.ok(tool);
 await admin.call('/locacao/admin/kits',verb({nome:`KIT ${nome}`,diaria:15,componentes:[{id:tool.id,quantidade:2}]},201));
 const kit=(await catalog()).find(i=>i.nome===`KIT ${nome}`);assert.ok(kit);
 const stock=async n=>assert.equal((await catalog()).find(i=>i.id===tool.id&&i.tipo==='FERRAMENTA').disponivel,n);
 const pedido=(extra={})=>({chave:randomUUID(),retirada:future(48),devolucao:future(72),recebimento:'RETIRADA',retorno:'NO_TECHHELP',itens:[{tipo:'FERRAMENTA',id:tool.id,quantidade:1}],...extra});
 const transport=pedido({recebimento:'ENTREGA',retorno:'COLETA',endereco:'ENDEREÇO FICTÍCIO EXCLUSIVO DO TESTE, não realizar entrega'});
 const r=await a.call('/locacao/pedidos',verb(transport));assert.equal(r.status,'AGUARDANDO_TAXA');assert.equal(r.total,null);await stock(2);
 assert.equal((await a.call('/locacao/pedidos',verb(transport))).id,r.id);
 await a.call('/locacao/pedidos',verb({...transport,endereco:transport.endereco+' alterado'},400));
 await b.call(`/locacao/pedidos/${r.id}/cancelar`,verb({},404));await b.call(`/locacao/pedidos/${r.id}/aceitar`,verb({},404));await a.call(`/locacao/pedidos/${r.id}/aceitar`,verb({},400));
 const quote=await admin.call(`/locacao/admin/pedidos/${r.id}/taxas`,verb({entrega:12,coleta:8,observacao:'Cotação de teste: distância fictícia, não realizar transporte'}));assert.equal(quote.total,30);await stock(2);
 await a.call(`/locacao/pedidos/${r.id}/aceitar`,verb({}));await stock(1);
 await a.call(`/locacao/pedidos/${r.id}/aceitar`,verb({}));await stock(1);
 await a.call('/locacao/pedidos',verb(pedido({itens:[{tipo:'KIT',id:kit.id,quantidade:1}]}),400));await stock(1);
 await admin.call(`/locacao/admin/pedidos/${r.id}/entregar`,verb({}));await a.call(`/locacao/pedidos/${r.id}/cancelar`,verb({},400));
 await admin.call(`/locacao/admin/pedidos/${r.id}/devolver`,verb({}));await stock(2);await admin.call(`/locacao/admin/pedidos/${r.id}/devolver`,verb({}));await stock(2);
 // Duas reservas concorrentes que pedem todo o estoque: só uma deve passar.
 const attempts=await Promise.all([a,b].map(async s=>{try{return await s.call('/locacao/pedidos',verb(pedido({itens:[{tipo:'KIT',id:kit.id,quantidade:1}]})));}catch(e){if(e.message.includes('recebido 400'))return null;throw e;}}));
 assert.equal(attempts.filter(Boolean).length,1);await stock(0);
 const winner=attempts.find(Boolean);await admin.call(`/locacao/admin/pedidos/${winner.id}/cancelar`,verb({}));await stock(2);await admin.call(`/locacao/admin/pedidos/${winner.id}/cancelar`,verb({}));await stock(2);
 await a.call('/locacao/pedidos',verb(pedido({devolucao:future(47)}),400));
 await admin.call(`/locacao/admin/ferramentas/${tool.id}/status`,verb({status:'INATIVA'}));
 console.log(`PASSOU: catálogo, permissões, transporte, aceite, kit/avulsa, concorrência, cancelamento e devolução. Execução ${run}. Registros mantidos no banco de testes; ferramenta inativada.`);
}catch(e){console.error(`FALHOU (execução ${run}): ${e.message}`);process.exitCode=1;}
