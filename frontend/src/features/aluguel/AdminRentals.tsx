import { useEffect, useRef, useState } from 'react';
import { api, errorMessage, type Session } from '../../services/api';
import { isAdmin, type Kit } from '../catalogo/catalog';
import { serviceDate } from '../servicos/services';
import { rentalStatus, type RentalItem } from './rentals';
import { loadAdminRentals, manageRental, overdue, type AdminRental, type RentalAction, type RentalPage } from './admin-rentals';
import './admin-rentals.css';

const money = (value: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
const failure = (e: unknown) => e instanceof Error && !('isAxiosError' in e) ? e.message : errorMessage(e);

export default function AdminRentals({ session, onBack }: { session: Session; onBack: () => void }) {
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [result, setResult] = useState<RentalPage | null>(null);
  const [items, setItems] = useState<Record<number, RentalItem[]>>({});
  const [kits, setKits] = useState<Kit[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [confirmation, setConfirmation] = useState<{ rental: AdminRental; action: RentalAction } | null>(null);
  const operation = useRef(false);

  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const [data, catalog] = await Promise.all([
          loadAdminRentals(session, page, controller.signal),
          api.get<Kit[]>('/kits', { signal: controller.signal }),
        ]);
        const details = await Promise.all(data.content.map(async ({ aluguel }) => [aluguel.idAluguel,
          (await api.get<RentalItem[]>('/alugueis/' + aluguel.idAluguel + '/itens', { signal: controller.signal })).data] as const));
        if (!controller.signal.aborted) { setResult(data); setKits(catalog.data); setItems(Object.fromEntries(details)); }
      } catch (e) { if (!controller.signal.aborted) setError(failure(e)); }
      finally { if (!controller.signal.aborted) setLoading(false); }
    }
    void load();
    return () => controller.abort();
  }, [session, page, revision]);

  function refresh(nextPage = page) { setConfirmation(null); setError(''); setLoading(true); setPage(nextPage); setRevision(v => v + 1); }
  async function confirm() {
    if (!confirmation || operation.current || loading || error) return;
    operation.current = true; setBusy(true); setNotice('');
    try {
      const saved = await manageRental(session, confirmation.rental, confirmation.action);
      setNotice('Aluguel #' + saved.idAluguel + ': ' + rentalStatus(saved.status) + '.');
      refresh();
    } catch (e) { setError(failure(e) + ' Atualize a lista antes de tentar novamente.'); setConfirmation(null); }
    finally { operation.current = false; setBusy(false); }
  }
  if (!isAdmin(session)) return <p role="alert">Acesso administrativo necessário.</p>;
  return <section className="th-wizard th-admin-rentals">
    <button className="th-link" disabled={busy} onClick={onBack}>← Voltar à Home</button>
    <h1 className="th-form-title">Administrar aluguéis</h1>
    <p>Registre a retirada após entregar o kit e a devolução após receber e conferir os equipamentos. Horários de Brasília.</p>
    <button className="th-button secondary" disabled={busy || loading} onClick={() => refresh()}>Atualizar aluguéis</button>
    {loading && <p role="status">Carregando aluguéis…</p>}
    {busy && <p role="status">Registrando operação…</p>}
    {notice && <p role="status">{notice}</p>}
    {error && <p role="alert">{error}</p>}
    {confirmation && <section className="th-note" aria-label="Confirmar operação">
      <h2>{confirmation.action === 'retirar' ? 'Registrar retirada' : 'Registrar devolução'} — aluguel #{confirmation.rental.idAluguel}</h2>
      <p>{confirmation.action === 'retirar' ? 'Confirme somente se os equipamentos foram entregues ao usuário.' : 'Confirme somente se os equipamentos foram recebidos e conferidos. A devolução libera a disponibilidade do kit.'}</p>
      <div className="th-actions"><button className="th-button secondary" disabled={busy} onClick={() => setConfirmation(null)}>Voltar</button><button className="th-button" disabled={busy} onClick={confirm}>Confirmar {confirmation.action === 'retirar' ? 'retirada' : 'devolução'}</button></div>
    </section>}
    {!loading && !error && result && <>
      {!result.content.length && <p>Nenhum aluguel nesta página.</p>}
      <div className="th-rental-cards">{result.content.map(({ aluguel: rental, nomeUsuario }) => <article key={rental.idAluguel}>
        <h2>Aluguel #{rental.idAluguel} · {rentalStatus(rental.status)}</h2>
        <p><strong>{nomeUsuario}</strong> · usuário #{rental.idUsuario}</p>
        {overdue(rental) && <p className="th-note">Devolução prevista vencida. Confira o retorno do equipamento.</p>}
        <ul>{(items[rental.idAluguel] ?? []).map(item => <li key={item.idKit}>{kits.find(k => k.idKit === item.idKit)?.nome ?? 'Kit #' + item.idKit} · {item.quantidade} unidade(s)</li>)}</ul>
        <p>Retirada prevista: {serviceDate(rental.dataPrevistaRetirada, 'Não informada')}<br />Devolução prevista: {serviceDate(rental.dataPrevistaDevolucao, 'Não informada')}</p>
        <p>Retirada registrada: {serviceDate(rental.dataRetirada, 'Ainda não realizada')}<br />Devolução registrada: {serviceDate(rental.dataDevolucao, 'Ainda não realizada')}</p>
        <p>Total registrado: {money(rental.valorTotal)}</p>
        {rental.status === 'RESERVADO' && <button className="th-button" disabled={busy || Boolean(confirmation)} onClick={() => setConfirmation({ rental, action: 'retirar' })}>Registrar retirada</button>}
        {['RETIRADO', 'ATRASADO'].includes(rental.status) && <button className="th-button" disabled={busy || Boolean(confirmation)} onClick={() => setConfirmation({ rental, action: 'devolver' })}>Registrar devolução</button>}
      </article>)}</div>
      <nav className="th-actions" aria-label="Páginas de aluguéis">
        <button className="th-button secondary" disabled={busy || Boolean(confirmation) || page === 0} onClick={() => refresh(page - 1)}>Anterior</button>
        <span>Página {result.number + 1} de {Math.max(1, result.totalPages)}</span>
        <button className="th-button secondary" disabled={busy || Boolean(confirmation) || page + 1 >= result.totalPages} onClick={() => refresh(page + 1)}>Próxima</button>
      </nav>
    </>}
  </section>;
}
