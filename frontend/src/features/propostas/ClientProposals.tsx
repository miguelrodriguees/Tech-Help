import { useEffect, useRef, useState } from 'react';
import { api, errorMessage, getSession, postProtected, type ServiceRequest } from '../../services/api';
import { dateLabel, money, openForProposals, statusLabel, type Proposal } from './proposals';

export default function ClientProposals({ idSolicitacao, idCliente, onBack }: { idSolicitacao: number; idCliente: number; onBack: () => void }) {
  const [request, setRequest] = useState<ServiceRequest | null>(null);
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [confirm, setConfirm] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);
  const [mustRefresh, setMustRefresh] = useState(false);
  const lock = useRef(false);

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      api.get<ServiceRequest>(`/solicitacoes/${idSolicitacao}`, { signal: controller.signal }),
      api.get<Proposal[]>(`/propostas/solicitacao/${idSolicitacao}`, { signal: controller.signal }),
    ]).then(([r, p]) => { setRequest(r.data); setProposals(p.data); setState('ready'); setMustRefresh(false); })
      .catch(failure => { if (!controller.signal.aborted) { setError(errorMessage(failure)); setState('error'); } });
    return () => controller.abort();
  }, [idSolicitacao, reload]);

  function refresh() { setConfirm(null); setError(''); setState('loading'); setReload(value => value + 1); }

  async function accept(proposal: Proposal) {
    if (lock.current || mustRefresh || confirm !== proposal.idProposta) return;
    lock.current = true; setBusy(true); setError('');
    try {
      const current = await getSession();
      if (current?.idCliente !== idCliente || !current.perfis.includes('CLIENTE')) { setError('A sessão mudou. Saia e entre novamente com a conta deste cliente.'); return; }
      const { data } = await postProtected<{ idServico: number }>('/servicos/aceitar-proposta/' + proposal.idProposta);
      setNotice(`Proposta aceita. Serviço #${data.idServico} criado como agendado. Combine os detalhes do atendimento com o técnico. As telas de execução serão integradas na próxima etapa.`);
      setRequest(previous => previous ? { ...previous, status: 'CONTRATADA' } : previous);
      setProposals(previous => previous.map(p => p.idProposta === proposal.idProposta ? { ...p, status: 'ACEITA' } : p.status === 'ENVIADA' ? { ...p, status: 'RECUSADA' } : p));
      setConfirm(null);
    } catch (failure) { setError(`${errorMessage(failure)} Atualize as propostas para conferir o resultado antes de tentar novamente.`); setMustRefresh(true); setConfirm(null); }
    finally { lock.current = false; setBusy(false); }
  }

  return <section className="th-wizard">
    <h1 className="th-form-title">Propostas do pedido #{idSolicitacao}</h1>
    <div className="th-actions"><button className="th-link" onClick={onBack} disabled={busy}>Voltar aos meus pedidos</button><button className="th-button secondary" onClick={refresh} disabled={busy || state === 'loading'}>Atualizar propostas</button></div>
    {state === 'loading' && <p role="status">Carregando propostas…</p>}
    {error && <p role="alert" className="th-note th-error">{error}</p>}
    {notice && <p role="status" className="th-note">{notice}</p>}
    {state === 'ready' && request && <>
      <h2>{request.titulo}</h2><p>Status do pedido: {statusLabel(request.status)}</p>
      <p className="th-sub">Compare o valor, as condições e a disponibilidade. Ao aceitar, as outras propostas enviadas serão recusadas.</p>
      {!proposals.length && <p>Nenhuma proposta recebida ainda. Consulte novamente mais tarde.</p>}
      <ul className="th-request-list">{proposals.map(p => <li className="th-flow-content" key={p.idProposta}>
        <h3>Técnico #{p.idTecnico} · {money(p.valor)}</h3>
        <p>{statusLabel(p.status)}</p>
        <dl className="th-review"><div><dt>Prazo estimado</dt><dd>{p.prazoEstimadoDias === null ? 'A combinar' : `${p.prazoEstimadoDias} dia(s)`}</dd></div><div><dt>Disponível a partir de</dt><dd>{dateLabel(p.dataDisponivel)}</dd></div><div><dt>Condições</dt><dd>{p.mensagem || 'Nenhuma condição adicional informada.'}</dd></div></dl>
        {p.status === 'ENVIADA' && openForProposals(request.status) && (confirm === p.idProposta ? <div className="th-note">
          <p>Confirmar a contratação do técnico #{p.idTecnico} por {money(p.valor)}? Esta ação aceita a proposta e cria o serviço. Não realiza pagamento.</p>
          <div className="th-actions"><button className="th-button" disabled={busy || mustRefresh} onClick={() => accept(p)}>{busy ? 'Confirmando…' : 'Confirmar aceite'}</button><button className="th-link" disabled={busy} onClick={() => setConfirm(null)}>Cancelar</button></div>
        </div> : <button className="th-button" disabled={busy || mustRefresh} onClick={() => setConfirm(p.idProposta)}>Escolher esta proposta</button>)}
      </li>)}</ul>
    </>}
  </section>;
}
