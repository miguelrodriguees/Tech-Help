import { useEffect, useRef, useState } from 'react';
import { api, errorMessage, type Session } from '../../services/api';
import { catalogPayload, editCatalogPayload, integerQuantity, isAdmin, saveCatalog, type Component, type Kit, type Tool } from './catalog';
import './catalog.css';

const money = (value: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
const failure = (error: unknown) => error instanceof Error && !('isAxiosError' in error) ? error.message : errorMessage(error);
type Save = (action: () => Promise<void>) => Promise<boolean>;

function CreateForm({ tool, disabled, save, session }: { tool: boolean; disabled: boolean; save: Save; session: Session }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState('');
  const [error, setError] = useState('');
  return <form onSubmit={async event => {
    event.preventDefault(); setError('');
    try {
      const body = catalogPayload(name, description, price, tool ? quantity : undefined);
      if (await save(() => saveCatalog(session, { kind: tool ? 'tool' : 'kit', body }))) {
        setName(''); setDescription(''); setPrice(''); setQuantity('');
      }
    } catch (e) { setError(failure(e)); }
  }}>
    <h2>{tool ? 'Cadastrar ferramenta' : 'Cadastrar kit'}</h2>
    <fieldset disabled={disabled}>
      <label className="th-label">Nome<input className="th-field" required maxLength={140} value={name} onChange={e => setName(e.target.value)} /></label>
      <label className="th-label">Descrição<textarea className="th-field" value={description} onChange={e => setDescription(e.target.value)} /></label>
      <label className="th-label">Diária (R$)<input className="th-field" required inputMode="decimal" placeholder="Ex.: 25,50" value={price} onChange={e => setPrice(e.target.value)} /></label>
      {tool && <label className="th-label">Quantidade total<input className="th-field" required type="number" min="0" max="2147483647" step="1" value={quantity} onChange={e => setQuantity(e.target.value)} /></label>}
      {error && <p role="alert">{error}</p>}
      <button className="th-button" type="submit">Cadastrar {tool ? 'ferramenta' : 'kit'}</button>
    </fieldset>
  </form>;
}

function EditForm({ item, disabled, save, session, onClose }: { item: Tool | Kit; disabled: boolean; save: Save; session: Session; onClose?: () => void }) {
  const tool = 'idFerramenta' in item;
  const [name, setName] = useState(item.nome);
  const [description, setDescription] = useState(item.descricao ?? '');
  const [price, setPrice] = useState(String(item.valorDiaria));
  const [quantity, setQuantity] = useState(tool ? String(item.quantidadeTotal) : '');
  const [status, setStatus] = useState(item.status);
  const [error, setError] = useState('');
  const [review, setReview] = useState<ReturnType<typeof editCatalogPayload> | null>(null);
  return <form onSubmit={event => {
    event.preventDefault(); setError('');
    try { setReview(editCatalogPayload(tool, name, description, price, status, quantity)); }
    catch (e) { setError(failure(e)); }
  }} className="th-catalog-edit">
    <h2>Editar {tool ? 'ferramenta' : 'kit'}: {item.nome}</h2>
    <p>Alterações ficam bloqueadas durante aluguel ativo. Inativar preserva o histórico. Novas diárias não alteram valores já registrados nos aluguéis.</p>
    <fieldset disabled={disabled || Boolean(review)}>
      <label className="th-label">Nome<input className="th-field" required maxLength={140} value={name} onChange={e => setName(e.target.value)} /></label>
      <label className="th-label">Descrição<textarea className="th-field" value={description} onChange={e => setDescription(e.target.value)} /></label>
      <label className="th-label">Diária (R$)<input className="th-field" required inputMode="decimal" value={price} onChange={e => setPrice(e.target.value)} /></label>
      {tool && <><label className="th-label">Quantidade total<input className="th-field" required type="number" min="0" max="2147483647" step="1" value={quantity} onChange={e => setQuantity(e.target.value)} /></label><p>Unidades anteriormente indisponíveis continuam indisponíveis ao ajustar o total. Manutenção impede novas reservas de todos os kits que usam esta ferramenta.</p></>}
      <label className="th-label">Situação<select className="th-field" value={status} onChange={e => setStatus(e.target.value)}>
        <option value="DISPONIVEL">Disponível para novas reservas</option>
        {tool ? <><option value="MANUTENCAO">Em manutenção</option><option value="INATIVA">Inativa</option></> : <><option value="INATIVO">Inativo</option>{item.status === 'INDISPONIVEL' && <option value="INDISPONIVEL" disabled>Indisponível — confira os aluguéis ativos</option>}</>}
      </select></label>
      <button className="th-button">Revisar alterações</button>
    </fieldset>
    {error && <p role="alert">{error}</p>}
    {review && <section className="th-note" aria-label="Confirmar alterações">
      <p>Salvar <strong>{review.nome}</strong>, diária {money(review.valorDiaria)} e situação {review.status}? {tool && 'Quantidade total: ' + quantity + '.'}</p>
      <div className="th-actions"><button className="th-button secondary" type="button" disabled={disabled} onClick={() => setReview(null)}>Voltar</button><button className="th-button" type="button" disabled={disabled} onClick={async () => {
        if (await save(() => saveCatalog(session, { kind: tool ? 'editTool' : 'editKit', id: tool ? item.idFerramenta : item.idKit, body: review }))) onClose?.();
      }}>Confirmar alterações</button></div>
    </section>}
    {onClose && <button className="th-link" type="button" disabled={disabled} onClick={onClose}>Fechar edição</button>}
  </form>;
}

function KitEditor({ kit, tools, disabled, save, session }: { kit: Kit; tools: Tool[]; disabled: boolean; save: Save; session: Session }) {
  const [items, setItems] = useState<Component[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toolId, setToolId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [confirm, setConfirm] = useState<number | null>(null);
  useEffect(() => {
    const controller = new AbortController();
    api.get<Component[]>('/kits/' + kit.idKit + '/ferramentas', { signal: controller.signal })
      .then(({ data }) => { if (!controller.signal.aborted) setItems(data); })
      .catch(e => { if (!controller.signal.aborted) setError(failure(e)); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [kit.idKit]);
  return <section aria-label={'Composição de ' + kit.nome}>
    <h2>Montar kit: {kit.nome}</h2>
    <p>A quantidade indica quantas unidades de cada ferramenta compõem uma unidade do kit. Kits com aluguel ativo não podem ser alterados.</p>
    {loading && <p role="status">Carregando composição…</p>}
    {error && <p role="alert">{error} Atualize o catálogo para tentar novamente.</p>}
    {!loading && !error && <>
      {!items.length && <p>Kit vazio: adicione ferramentas para permitir reservas.</p>}
      <ul className="th-catalog-list">{items.map(item => <li key={item.idFerramenta}>
        <strong>{tools.find(t => t.idFerramenta === item.idFerramenta)?.nome ?? 'Ferramenta #' + item.idFerramenta}</strong> — {item.quantidade} unidade(s)
        {confirm === item.idFerramenta ? <div><p>Remover esta ferramenta do kit?</p><button className="th-button secondary" disabled={disabled} onClick={() => setConfirm(null)}>Manter</button> <button className="th-button" disabled={disabled} onClick={() => save(() => saveCatalog(session, { kind: 'remove', kitId: kit.idKit, toolId: item.idFerramenta }))}>Confirmar remoção</button></div> : <button className="th-link" disabled={disabled} onClick={() => setConfirm(item.idFerramenta)}>Remover do kit</button>}
      </li>)}</ul>
      <form onSubmit={async event => {
        event.preventDefault();
        try {
          const amount = integerQuantity(quantity);
          await save(() => saveCatalog(session, { kind: 'component', kitId: kit.idKit, toolId: Number(toolId), quantity: amount }));
        } catch (e) { setError(failure(e)); }
      }}><fieldset disabled={disabled}>
        <label className="th-label">Ferramenta<select className="th-field" required value={toolId} onChange={e => setToolId(e.target.value)}><option value="">Selecione</option>{tools.filter(t => !items.some(i => i.idFerramenta === t.idFerramenta)).map(t => <option key={t.idFerramenta} value={t.idFerramenta}>{t.nome} — total: {t.quantidadeTotal}</option>)}</select></label>
        <label className="th-label">Unidades por kit<input className="th-field" required type="number" min="1" max="2147483647" step="1" value={quantity} onChange={e => setQuantity(e.target.value)} /></label>
        <button className="th-button">Adicionar ao kit</button>
      </fieldset></form>
    </>}
  </section>;
}

export default function AdminCatalog({ session, onBack }: { session: Session; onBack: () => void }) {
  const [tools, setTools] = useState<Tool[]>([]);
  const [kits, setKits] = useState<Kit[]>([]);
  const [selected, setSelected] = useState('');
  const [editingTool, setEditingTool] = useState<number | null>(null);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [blocked, setBlocked] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const operation = useRef(false);
  useEffect(() => {
    if (!isAdmin(session)) return;
    const controller = new AbortController();
    Promise.all([api.get<Tool[]>('/ferramentas', { signal: controller.signal }), api.get<Kit[]>('/kits', { signal: controller.signal })])
      .then(([t, k]) => { if (!controller.signal.aborted) { setTools(t.data); setKits(k.data); setBlocked(false); } })
      .catch(e => { if (!controller.signal.aborted) { setError(failure(e)); setBlocked(true); } })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [revision, session]);
  function reload() { setError(''); setLoading(true); setRevision(v => v + 1); }
  const save: Save = async action => {
    if (operation.current || loading || blocked) return false;
    operation.current = true; setBusy(true); setError(''); setMessage('');
    try { await action(); setMessage('Operação confirmada.'); reload(); return true; }
    catch (e) { setError(failure(e) + ' Atualize o catálogo e confira o resultado antes de tentar novamente.'); setBlocked(true); return false; }
    finally { operation.current = false; setBusy(false); }
  };
  if (!isAdmin(session)) return <p role="alert">Área exclusiva para administradores.</p>;
  const kit = kits.find(k => k.idKit === Number(selected));
  const disabled = busy || loading || blocked;
  return <section className="th-wizard th-catalog">
    <button className="th-link" disabled={busy} onClick={onBack}>← Voltar à Home</button>
    <h1 className="th-form-title">Catálogo administrativo</h1>
    <p>Cadastre as ferramentas do Tech Help e monte os kits para aluguel.</p>
    <button className="th-button secondary" disabled={busy || loading} onClick={reload}>Atualizar catálogo</button>
    {loading && <p role="status">Carregando catálogo…</p>}
    {busy && <p role="status">Salvando…</p>}
    {message && <p role="status">{message}</p>}
    {error && <p role="alert">{error}</p>}
    <div className="th-catalog-grid">
      <CreateForm tool disabled={disabled} save={save} session={session} />
      <CreateForm tool={false} disabled={disabled} save={save} session={session} />
    </div>
    {!loading && !blocked && <>
      <h2>Ferramentas cadastradas ({tools.length})</h2>
      <p>O total cadastrado não representa o saldo livre: reservas ativas também comprometem as ferramentas.</p>
      {!tools.length && <p>Nenhuma ferramenta cadastrada.</p>}
      <ul className="th-catalog-list">{tools.map(t => <li key={t.idFerramenta}><strong>{t.nome}</strong><p>{t.descricao}</p><p>Total: {t.quantidadeTotal} · Diária: {money(t.valorDiaria)} · Status: {t.status}</p><button className="th-button secondary" disabled={disabled} onClick={() => setEditingTool(t.idFerramenta)}>Editar ferramenta</button>{editingTool === t.idFerramenta && <EditForm key={t.idFerramenta + ':' + revision} item={t} disabled={disabled} save={save} session={session} onClose={() => setEditingTool(null)} />}</li>)}</ul>
      <h2>Kits cadastrados ({kits.length})</h2>
      {!kits.length ? <p>Nenhum kit cadastrado. Use o formulário acima para começar.</p> : <>
        <label className="th-label">Selecione um kit para ver e montar sua composição<select className="th-field" disabled={busy} value={selected} onChange={e => setSelected(e.target.value)}><option value="">Selecione</option>{kits.map(k => <option key={k.idKit} value={k.idKit}>{k.nome} — {money(k.valorDiaria)}/dia — {k.status}</option>)}</select></label>
        {kit && <><EditForm key={'edit:' + kit.idKit + ':' + revision} item={kit} disabled={disabled} save={save} session={session} /><KitEditor key={kit.idKit + ':' + revision} kit={kit} tools={tools} disabled={disabled} save={save} session={session} /></>}
      </>}
    </>}
  </section>;
}
