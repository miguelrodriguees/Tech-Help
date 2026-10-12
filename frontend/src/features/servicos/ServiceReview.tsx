import { useRef, useState } from 'react';
import { api, errorMessage, type Session } from '../../services/api';
import type { Service, ServiceView } from './services';
import { submitReview, type Review } from './reviews';

export default function ServiceReview({ service, session, view }: { service: Service; session: Session; view: ServiceView }) {
  const [state, setState] = useState<'idle' | 'loading' | 'ready' | 'error'>('idle');
  const [review, setReview] = useState<Review | null>(null);
  const [nota, setNota] = useState(0);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [busy, setBusy] = useState(false);
  const lock = useRef(false);

  async function load() {
    if (lock.current) return;
    lock.current = true; setState('loading'); setError('');
    try {
      const { data } = await api.get<Review[]>(`/avaliacoes/servico/${service.idServico}`);
      setReview(data.find(item => view === 'cliente' ? item.idAvaliador === session.idUsuario : item.idAvaliado === session.idUsuario) ?? null);
      setState('ready');
    } catch (failure) { setError(errorMessage(failure)); setState('error'); }
    finally { lock.current = false; }
  }

  async function send(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (lock.current || state !== 'ready' || review) return;
    lock.current = true; setBusy(true); setError('');
    try { setReview(await submitReview(service, session, nota, comment)); setSuccess(true); }
    catch (failure) {
      const message = failure instanceof Error && !('isAxiosError' in failure) ? failure.message : errorMessage(failure);
      setError(`${message} Consulte a avaliação antes de tentar enviar novamente; suas respostas foram mantidas.`);
      setState('error');
    } finally { lock.current = false; setBusy(false); }
  }

  return <div className="th-note">
    <h3>{view === 'cliente' ? 'Sua avaliação do atendimento' : 'Avaliação recebida'}</h3>
    {state === 'idle' && <button className="th-button secondary" onClick={load}>{view === 'cliente' ? 'Avaliar ou consultar avaliação' : 'Consultar avaliação'}</button>}
    {state === 'loading' && <p role="status">Consultando avaliação…</p>}
    {error && <p role="alert" className="th-error">{error}</p>}
    {state === 'error' && <button className="th-button secondary" onClick={load}>Consultar novamente</button>}
    {state === 'ready' && (review ? <>
      {success && <p role="status">Avaliação enviada com sucesso!</p>}
      <p><strong>Nota: {review.nota} de 5</strong></p><p className="th-request-description">{review.comentario || 'Sem comentário.'}</p>
      <p>A avaliação já foi registrada.</p>
    </> : view === 'cliente' ? <form onSubmit={send}>
      <fieldset className="th-auth-fields" disabled={busy}>
        <legend>Como foi o atendimento?</legend>
        <label className="th-label">Nota<select className="th-field" required value={nota || ''} onChange={e => setNota(Number(e.target.value))}><option value="" disabled>Escolha uma nota</option>{[1, 2, 3, 4, 5].map(value => <option key={value} value={value}>{value} de 5{value === 1 ? ' — Muito ruim' : value === 5 ? ' — Excelente' : ''}</option>)}</select></label>
        <label className="th-label">Comentário (opcional)<textarea className="th-field" maxLength={3000} value={comment} onChange={e => setComment(e.target.value)} placeholder="Conte como foi o atendimento, sem incluir dados pessoais." /></label>
        <p>{comment.length}/3000 caracteres. Confira antes de enviar: a avaliação não poderá ser editada nesta tela.</p>
        <button className="th-button" type="submit" disabled={!nota || busy}>{busy ? 'Enviando…' : 'Enviar avaliação'}</button>
      </fieldset>
    </form> : <><p>O cliente ainda não registrou uma avaliação.</p><button className="th-link" onClick={load}>Consultar novamente</button></>)}
  </div>;
}
