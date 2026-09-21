import test from 'node:test';
import assert from 'node:assert/strict';
import { emptyDraft } from '../src/features/solicitacao/flow.ts';
import { toSolicitacaoPayload } from '../src/features/solicitacao/payload.ts';
const draft=()=>({...emptyDraft(),area:'Hardware',categoryId:1,mode:'Presencial',urgency:'Próximos dias',city:'Taboão da Serra, SP',district:'Centro',branches:{Hardware:{equipment:'Notebook',problem:'Não liga',brand:'Dell'},Redes:{problem:'Resposta antiga'}}});
test('Pedido não envia identidade de cliente nem respostas de outra categoria',()=>{const r=toSolicitacaoPayload(draft());assert.equal(r.ok,true);assert.equal('idCliente' in r.payload,false);assert.equal(r.payload.descricao.includes('Resposta antiga'),false);assert.match(r.payload.descricao,/Taboão/);assert.equal(r.payload.urgencia,'NORMAL');});
test('Atendimento remoto não envia cidade ou bairro do rascunho',()=>{const r=toSolicitacaoPayload({...draft(),mode:'Remoto'});assert.equal(r.ok,true);assert.equal(r.payload.descricao.includes('Taboão'),false);assert.equal(r.payload.tipoAtendimento,'REMOTO');});
test('Não monta pedido sem categoria ou atendimento',()=>{assert.equal(toSolicitacaoPayload(emptyDraft()).ok,false);assert.equal(toSolicitacaoPayload({...draft(),mode:''}).ok,false);});
