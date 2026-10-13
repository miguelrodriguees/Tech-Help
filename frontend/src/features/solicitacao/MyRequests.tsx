import { useEffect, useState } from 'react';
import { api, errorMessage, type ServiceRequest } from '../../services/api';
import ClientProposals from '../propostas/ClientProposals';

export default function MyRequests({ idCliente, onBack, uncertain }: { idCliente: number; onBack: () => void; uncertain: boolean }) {
  const [requests, setRequests] = useState<ServiceRequest[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  const [selected, setSelected] = useState<number | null>(null);
  useEffect(() => {
    const controller = new AbortController();
    api.get<ServiceRequest[]>(`/solicitacoes/cliente/${idCliente}`, { signal: controller.signal })
      .then(response => { setRequests(response.data); setState('ready'); })
      .catch(failure => { if (!controller.signal.aborted) { setError(errorMessage(failure)); setState('error'); } });
    return () => controller.abort();
  }, [idCliente, retry]);
  if (selected !== null) return <ClientProposals key={selected} idSolicitacao={selected} idCliente={idCliente} onBack={() => { setSelected(null); setState('loading'); setRetry(value => value + 1); }} />;
  return <section className="th-wizard">
    <h1 className="th-form-title">Meus pedidos</h1>
    {uncertain && <p className="th-note">O envio anterior ficou sem confirmação. Confira se seu pedido aparece abaixo antes de tentar publicar novamente. Se ele aparecer, já foi recebido.</p>}
    {state === 'loading' && <p role="status">Carregando seus pedidos…</p>}
    {state === 'error' && <p role="alert" className="th-note th-error">{error}</p>}
    <button className="th-link" disabled={state === 'loading'} onClick={() => { setState('loading'); setRetry(value => value + 1); }}>Atualizar pedidos</button>
    {state === 'ready' && (requests.length ? <ul className="th-request-list">{requests.map(request => <li className="th-flow-content" key={request.idSolicitacao}><h2>#{request.idSolicitacao} · {request.titulo}</h2><p>Status: {request.status.replaceAll('_', ' ')}</p><details><summary>Ver descrição enviada</summary><p className="th-request-description">{request.descricao}</p></details><button className="th-button secondary" onClick={() => setSelected(request.idSolicitacao)}>Ver propostas</button></li>)}</ul> : <p>Nenhum pedido publicado nesta conta.</p>)}
    <button className="th-button secondary" disabled={uncertain && state !== 'ready'} onClick={onBack}>Voltar</button>
  </section>;
}
