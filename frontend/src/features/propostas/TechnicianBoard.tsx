import { useEffect, useRef, useState, type FormEvent } from 'react';
import { api, errorMessage, getSession, postProtected, type ServiceRequest } from '../../services/api';
import { dateLabel, money, proposalPayload, statusLabel, type Proposal } from './proposals';

export default function TechnicianBoard({ idTecnico, onBack }: { idTecnico: number; onBack: () => void }) {
  const [requests, setRequests] = useState<ServiceRequest[]>([]);
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [reload, setReload] = useState(0);
  const [selected, setSelected] = useState<number | null>(null);
  const [busy, setBusy] = useState(false);
  const [mustRefresh, setMustRefresh] = useState(false);
  const lock = useRef(false);

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      api.get<ServiceRequest[]>('/solicitacoes/abertas', { signal: controller.signal }),
      api.get<Proposal[]>(`/propostas/tecnico/${idTecnico}`, { signal: controller.signal }),
    ]).then(([available, sent]) => { setRequests(available.data); setProposals(sent.data); setState('ready'); setMustRefresh(false); })
      .catch(failure => { if (!controller.signal.aborted) { setError(errorMessage(failure)); setState('error'); } });
    return () => controller.abort();
  }, [idTecnico, reload]);

  function refresh() { setError(''); setState('loading'); setReload(value => value + 1); }

  async function send(event: FormEvent<HTMLFormElement>, idSolicitacao: number) {
    event.preventDefault();
    if (lock.current || mustRefresh) return;
    const data = new FormData(event.currentTarget);
    let payload;
    try { payload = proposalPayload(idSolicitacao, idTecnico, { valor: String(data.get('valor')), mensagem: String(data.get('mensagem')), prazo: String(data.get('prazo')), data: String(data.get('data')) }); }
    catch (failure) { setError((failure as Error).message); return; }
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try {
      const current = await getSession();
      if (current?.idTecnico !== idTecnico || !current.perfis.includes('TECNICO')) {
        setError('A sessão mudou. Saia e entre novamente com a conta de técnico.'); return;
      }
      const { data: proposal } = await postProtected<Proposal>('/propostas', payload);
      setProposals(previous => [proposal, ...previous]); setSelected(null);
      setNotice(`Proposta enviada para o pedido #${idSolicitacao}. Aguarde a decisão do cliente.`);
    } catch (failure) {
      setError(`${errorMessage(failure)} Atualize a lista para conferir se a proposta já foi recebida antes de tentar novamente.`);
      setMustRefresh(true);
    } finally { lock.current = false; setBusy(false); }
  }

  return <section className="th-wizard">
    <h1 className="th-form-title">Área do técnico</h1>
    <p className="th-sub">Confira os pedidos disponíveis e envie suas condições de atendimento.</p>
    <div className="th-actions"><button className="th-button secondary" disabled={busy || state === 'loading'} onClick={refresh}>Atualizar oportunidades e propostas</button><button className="th-link" disabled={busy} onClick={onBack}>Voltar à Home</button></div>
    {state === 'loading' && <p role="status">Carregando oportunidades…</p>}
    {error && <p className="th-note th-error" role="alert">{error}</p>}
    {notice && <p className="th-note" role="status">{notice}</p>}
    {state === 'ready' && <>
      <h2>Solicitações disponíveis</h2>
      {!requests.length && <p>Nenhuma solicitação disponível no momento.</p>}
      <ul className="th-request-list">{requests.map(request => {
        const existing = proposals.find(p => p.idSolicitacao === request.idSolicitacao);
        return <li className="th-flow-content" key={request.idSolicitacao}>
          <h3>#{request.idSolicitacao} · {request.titulo}</h3>
          <p>{statusLabel(request.status)}</p>
          <details><summary>Ver necessidade do cliente</summary><p className="th-request-description">{request.descricao}</p></details>
          {existing ? <p className="th-note">Você já enviou uma proposta: {money(existing.valor)} · {statusLabel(existing.status)}</p> : <>
            <button className="th-button secondary" disabled={busy || mustRefresh} onClick={() => { setSelected(selected === request.idSolicitacao ? null : request.idSolicitacao); setError(''); }}>{selected === request.idSolicitacao ? 'Fechar formulário' : 'Preparar proposta'}</button>
            {selected === request.idSolicitacao && <form onSubmit={event => send(event, request.idSolicitacao)}>
              <fieldset className="th-auth-fields" disabled={busy || mustRefresh}>
                <label className="th-label">Valor total (R$)<input className="th-field" name="valor" required inputMode="decimal" placeholder="Ex.: 150,00" /></label>
                <label className="th-label">Condições do atendimento <span>(opcional)</span><textarea className="th-field" name="mensagem" maxLength={3000} placeholder="Explique o que está incluído e o que precisa combinar." /></label>
                <label className="th-label">Prazo estimado em dias <span>(opcional)</span><input className="th-field" name="prazo" type="number" min={0} max={32767} step={1} /></label>
                <label className="th-label">Disponível a partir de <span>(opcional)</span><input className="th-field" name="data" type="date" /></label>
                <p className="th-footnote">O cliente poderá comparar sua proposta. O atendimento só é contratado após o aceite.</p>
                <button className="th-button" type="submit">{busy ? 'Enviando…' : 'Enviar proposta'}</button>
              </fieldset>
            </form>}
          </>}
        </li>;
      })}</ul>
      <h2>Minhas propostas</h2>
      {!proposals.length && <p>Você ainda não enviou propostas.</p>}
      <ul className="th-request-list">{proposals.map(p => <li className="th-flow-content" key={p.idProposta}>
        <h3>Pedido #{p.idSolicitacao} · {requests.find(r => r.idSolicitacao === p.idSolicitacao)?.titulo ?? 'Proposta enviada'}</h3>
        <p>{money(p.valor)} · {statusLabel(p.status)}</p>
        <p>Prazo: {p.prazoEstimadoDias === null ? 'A combinar' : `${p.prazoEstimadoDias} dia(s)`} · Disponibilidade: {dateLabel(p.dataDisponivel)}</p>
        <p className="th-request-description">{p.mensagem || 'Sem condições adicionais.'}</p>
        {p.status === 'ACEITA' && <p className="th-note">O cliente aceitou sua proposta. As telas de execução do serviço serão integradas na próxima etapa.</p>}
      </li>)}</ul>
    </>}
  </section>;
}
