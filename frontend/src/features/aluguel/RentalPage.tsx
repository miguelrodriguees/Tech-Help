import { useEffect, useRef, useState } from 'react';
import { api, errorMessage, type Session } from '../../services/api';
import type { Kit } from '../catalogo/catalog';
import { serviceDate } from '../servicos/services';
import { cancelRental, estimate, loadRentals, rentalStatus, reservationPayload, reserve, type Rental, type RentalItem } from './rentals';

const money = (n: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(n);
const messageOf = (e: unknown) => e instanceof Error && !('isAxiosError' in e) ? e.message : errorMessage(e);
type Confirmation = { kind: 'reserve'; body: ReturnType<typeof reservationPayload>; name: string; total: number; days: number } | { kind: 'cancel'; rental: Rental };

export default function Rentals({ session, onBack, onLogin }: { session: Session | null; onBack: () => void; onLogin: () => void }) {
  const [kits, setKits] = useState<Kit[]>([]);
  const [available, setAvailable] = useState<Kit[]>([]);
  const [rentals, setRentals] = useState<Rental[]>([]);
  const [items, setItems] = useState<Record<number, RentalItem[]>>({});
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [blocked, setBlocked] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [kitId, setKitId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null);
  const operation = useRef(false);

  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const [all, free, own] = await Promise.all([
          api.get<Kit[]>('/kits', { signal: controller.signal }),
          api.get<Kit[]>('/kits/disponiveis', { signal: controller.signal }),
          session ? loadRentals(session, controller.signal) : Promise.resolve([]),
        ]);
        const details = await Promise.all(own.map(async rental => [
          rental.idAluguel,
          (await api.get<RentalItem[]>('/alugueis/' + rental.idAluguel + '/itens', { signal: controller.signal })).data,
        ] as const));
        if (!controller.signal.aborted) {
          setKits(all.data); setAvailable(free.data); setRentals(own); setItems(Object.fromEntries(details)); setBlocked(false);
        }
      } catch (e) { if (!controller.signal.aborted) { setError(messageOf(e)); setBlocked(true); } }
      finally { if (!controller.signal.aborted) setLoading(false); }
    }
    void load();
    return () => controller.abort();
  }, [revision, session]);

  function reload() { setConfirmation(null); setLoading(true); setError(''); setRevision(v => v + 1); }
  async function confirm() {
    if (operation.current || !session || !confirmation || blocked || loading) return;
    operation.current = true; setBusy(true); setError(''); setNotice('');
    try {
      if (confirmation.kind === 'reserve') {
        const saved = await reserve(session, confirmation.body);
        setNotice('Reserva #' + saved.idAluguel + ' confirmada. Total: ' + money(saved.valorTotal) + '.');
        setKitId(''); setQuantity('1'); setStart(''); setEnd('');
      } else {
        await cancelRental(session, confirmation.rental); setNotice('Reserva cancelada.');
      }
      reload();
    } catch (e) {
      setError(messageOf(e) + ' Atualize e confira Meus aluguéis antes de tentar novamente.');
      setBlocked(true); setConfirmation(null);
    } finally { operation.current = false; setBusy(false); }
  }
  return <section className="th-wizard">
    <button className="th-link" disabled={busy} onClick={onBack}>← Voltar à Home</button>
    <h1 className="th-form-title">Aluguel de kits</h1>
    <p>Retirada e devolução no Tech Help. O pagamento é combinado diretamente com o Tech Help, fora do site. Entrega não está disponível nesta versão.</p>
    <button className="th-button secondary" disabled={loading || busy} onClick={reload}>Atualizar catálogo e aluguéis</button>
    {loading && <p role="status">Carregando…</p>}
    {busy && <p role="status">Confirmando operação…</p>}
    {notice && <p role="status">{notice}</p>}
    {error && <p role="alert">{error}</p>}
    {!loading && !blocked && <>
      <h2>Kits disponíveis</h2>
      {!available.length && <p>Nenhum kit disponível neste momento.</p>}
      <ul>{available.map(kit => <li key={kit.idKit}><strong>{kit.nome}</strong> — {money(kit.valorDiaria)}/dia<p>{kit.descricao}</p></li>)}</ul>
      {!session ? <><p>Entre na sua conta para reservar e acompanhar seus aluguéis.</p><button className="th-button" onClick={onLogin}>Entrar para reservar</button></> : <>
        {available.length > 0 && <form onSubmit={event => {
          event.preventDefault(); setError(''); setNotice('');
          try {
            const kit = available.find(k => k.idKit === Number(kitId));
            if (!kit) throw new Error('Selecione um kit disponível.');
            const body = reservationPayload(session, kit.idKit, quantity, start, end);
            setConfirmation({ kind: 'reserve', body, name: kit.nome, ...estimate(kit, body.quantidade, start, end) });
          } catch (e) { setError(messageOf(e)); }
        }}>
          <fieldset disabled={busy || Boolean(confirmation)} style={{ border: 0, padding: 0 }}>
            <label className="th-label">Kit<select className="th-field" required value={kitId} onChange={e => setKitId(e.target.value)}><option value="">Selecione</option>{available.map(k => <option key={k.idKit} value={k.idKit}>{k.nome}</option>)}</select></label>
            <label className="th-label">Quantidade de kits<input className="th-field" type="number" min="1" max="2147483647" step="1" required value={quantity} onChange={e => setQuantity(e.target.value)} /></label>
            <label className="th-label">Retirada prevista (horário de Brasília)<input className="th-field" type="datetime-local" required value={start} onChange={e => setStart(e.target.value)} /></label>
            <label className="th-label">Devolução prevista (horário de Brasília)<input className="th-field" type="datetime-local" required value={end} onChange={e => setEnd(e.target.value)} /></label>
            <button className="th-button">Revisar reserva</button>
          </fieldset>
        </form>}
        {confirmation && <section aria-label="Confirmar operação">
          <h2>{confirmation.kind === 'reserve' ? 'Confira sua reserva' : 'Cancelar reserva #' + confirmation.rental.idAluguel + '?'}</h2>
          {confirmation.kind === 'reserve' && <>
            <p>{confirmation.name} · {confirmation.body.quantidade} unidade(s) · {confirmation.days} diária(s)</p>
            <p>Retirada: {serviceDate(confirmation.body.dataPrevistaRetirada, '')}<br />Devolução: {serviceDate(confirmation.body.dataPrevistaDevolucao, '')}</p>
            <p>Total estimado: <strong>{money(confirmation.total)}</strong>. Valor e estoque serão confirmados ao reservar.</p>
            <p>O valor considera a diferença entre as datas, com mínimo de uma diária. A reserva compromete o kit até o cancelamento ou a devolução. O pagamento é combinado diretamente com o Tech Help, fora do site; confirmar a reserva não realiza uma cobrança online.</p>
          </>}
          <div className="th-actions"><button className="th-button secondary" disabled={busy} onClick={() => setConfirmation(null)}>Voltar</button><button className="th-button" disabled={busy} onClick={confirm}>{confirmation.kind === 'reserve' ? 'Confirmar reserva' : 'Confirmar cancelamento'}</button></div>
        </section>}
        <h2>Meus aluguéis</h2>
        {!rentals.length && <p>Você ainda não tem aluguéis.</p>}
        {rentals.map(rental => <article key={rental.idAluguel}>
          <h3>Aluguel #{rental.idAluguel} · {rentalStatus(rental.status)}</h3>
          <ul>{(items[rental.idAluguel] ?? []).map(item => <li key={item.idKit}>{kits.find(k => k.idKit === item.idKit)?.nome ?? 'Kit #' + item.idKit} · {item.quantidade} unidade(s)</li>)}</ul>
          <p>Retirada prevista: {serviceDate(rental.dataPrevistaRetirada, 'Não informada')}<br />Devolução prevista: {serviceDate(rental.dataPrevistaDevolucao, 'Não informada')}</p>
          <p>Total registrado: {money(rental.valorTotal)}</p>
          {rental.status === 'RESERVADO' && <button className="th-button secondary" disabled={busy || Boolean(confirmation)} onClick={() => { setError(''); setConfirmation({ kind: 'cancel', rental }); }}>Cancelar reserva</button>}
        </article>)}
      </>}
    </>}
  </section>;
}
