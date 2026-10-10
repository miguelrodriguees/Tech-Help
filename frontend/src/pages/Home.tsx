import { useEffect, useRef, useState } from 'react';
import { ArrowRight, CircleHelp, Menu, Pause, Play, Wrench, X } from 'lucide-react';
import { api, errorMessage, getSession, postProtected, statusOf, type Session, type ServiceRequest } from '../services/api';
import AuthDialog from '../features/auth/AuthDialog';
import MyRequests from '../features/solicitacao/MyRequests';
import TechnicianBoard from '../features/propostas/TechnicianBoard';
import { requestPayload } from '../features/solicitacao/publication';
import type { Categoria } from '../types/Categoria';
import RequestAssistant from '../features/solicitacao/RequestAssistant';
import { areaFor, draftKey, emptyDraft, parseDraft, type Draft } from '../features/solicitacao/flow';
import Landing from '../features/home/Landing';

type Catalog = { status: 'loading' | 'ready' | 'error'; categories: Categoria[] };
type Screen = 'home' | 'categories' | 'unsure' | 'request' | 'requests' | 'published' | 'technician';
const navigation = [['servicos', 'Serviços'], ['como-funciona', 'Como funciona'], ['ferramentas', 'Aluguel de ferramentas'], ['profissionais', 'Sou profissional']];
function loadDraft(): Draft {
  try { return parseDraft(sessionStorage.getItem(draftKey)); }
  catch { return emptyDraft(); }
}

export default function Home() {
  const [catalog, setCatalog] = useState<Catalog>({ status: 'loading', categories: [] });
  const [reload, setReload] = useState(0);
  const [draft, setDraft] = useState<Draft>(loadDraft);
  const [saved, setSaved] = useState(() => { try { return sessionStorage.getItem(draftKey) !== null; } catch { return false; } });
  const [screen, setScreen] = useState<Screen>('home');
  const [menu, setMenu] = useState(false);
  const [paused, setPaused] = useState(false);
  const [message, setMessage] = useState({ title: '', body: '' });
  const dialog = useRef<HTMLDialogElement>(null);
  const main = useRef<HTMLElement>(null);
  const [session, setSession] = useState<Session | null>(null);
  const [sessionLoading, setSessionLoading] = useState(true);
  const [sessionError, setSessionError] = useState('');
  const [authOpen, setAuthOpen] = useState(false);
  const [authProfessional, setAuthProfessional] = useState(false);
  const [busy, setBusy] = useState(false);
  const [publishError, setPublishError] = useState('');
  const [uncertain, setUncertain] = useState(false);
  const [published, setPublished] = useState<ServiceRequest | null>(null);
  const operation = useRef(false);

  useEffect(() => {
    let active = true;
    getSession().then(value => { if (active) setSession(value); })
      .catch(() => { if (active) setSessionError('Não foi possível verificar sua conta. Use Entrar para tentar novamente.'); })
      .finally(() => { if (active) setSessionLoading(false); });
    return () => { active = false; };
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    api.get<Categoria[]>('/categorias', { signal: controller.signal }).then(response => {
      if (!Array.isArray(response.data) || response.data.some(c => !c || !Number.isSafeInteger(c.idCategoria) || c.idCategoria <= 0 || typeof c.nome !== 'string' || typeof c.ativo !== 'boolean')) throw new Error('Catálogo inválido');
      setCatalog({ status: 'ready', categories: response.data.filter(c => c.ativo) });
    }).catch(() => { if (!controller.signal.aborted) setCatalog({ status: 'error', categories: [] }); });
    return () => controller.abort();
  }, [reload]);
  useEffect(() => { if (screen !== 'home') main.current?.focus({ preventScroll: true }); }, [screen]);

  function navigate(next: Screen) {
    if (operation.current && next !== 'published') return;
    setMenu(false); setScreen(next); window.scrollTo({ top: 0, behavior: 'instant' });
  }
  function notify(title: string, body: string) { setMessage({ title, body }); dialog.current?.showModal(); }
  function updateDraft(next: Draft) {
    setDraft(next);
    try { sessionStorage.setItem(draftKey, JSON.stringify(next)); setSaved(true); }
    catch { setSaved(false); }
  }
  function retry() { setCatalog({ status: 'loading', categories: [] }); setReload(value => value + 1); }
  function choose(category: Categoria) {
    const area = areaFor(category.nome);
    if (!area) { notify(category.nome, 'As perguntas desta área ainda estão em preparação. Por enquanto, o assistente está disponível para Hardware, Redes e Software.'); return; }
    updateDraft({ ...draft, categoryId: category.idCategoria, area, mode: draft.area === area ? draft.mode : '' });
    navigate('request');
  }
  function resume() {
    const category = catalog.categories.find(c => c.idCategoria === draft.categoryId && areaFor(c.nome) === draft.area);
    if (catalog.status !== 'ready' || !category) { navigate('categories'); notify('Confira a categoria do pedido', 'Precisamos de uma categoria disponível para continuar. Suas respostas anteriores permanecem guardadas.'); return; }
    navigate('request');
  }
  function sectionLink(id: string) {
    if (operation.current) return;
    setMenu(false); setScreen('home');
    requestAnimationFrame(() => document.getElementById(id)?.scrollIntoView({ behavior: paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' }));
  }
  async function logout() {
    if (operation.current) return;
    operation.current = true; setBusy(true);
    try { await postProtected('/auth/logout'); setSession(null); setScreen('home'); setMenu(false); }
    catch (error) { notify('Não foi possível sair', errorMessage(error)); }
    finally { operation.current = false; setBusy(false); }
  }
  async function publish() {
    if (operation.current || uncertain) return;
    setPublishError('');
    if (!session) { setAuthOpen(true); return; }
    operation.current = true; setBusy(true);
    let sent = false;
    try {
      const current = await getSession(); setSession(current);
      if (!current) { setAuthOpen(true); return; }
      if (!current.idCliente || !current.perfis.includes('CLIENTE')) { setPublishError('A publicação exige uma conta de cliente. Saia e entre com uma conta de cliente; seu rascunho será preservado.'); return; }
      const payload = requestPayload(draft, current.idCliente);
      if (!catalog.categories.some(c => c.idCategoria === draft.categoryId && areaFor(c.nome) === draft.area)) { setPublishError('Escolha uma categoria disponível antes de publicar.'); return; }
      sent = true;
      const { data } = await postProtected<ServiceRequest>('/solicitacoes', payload);
      setPublished(data); setDraft(emptyDraft()); setSaved(false);
      try { sessionStorage.removeItem(draftKey); } catch { /* O pedido já foi confirmado. */ }
      navigate('published');
    } catch (error) {
      if (statusOf(error) === 401) { setSession(null); setAuthOpen(true); }
      const unknown = sent && (statusOf(error) === undefined || (statusOf(error) ?? 0) >= 500);
      setUncertain(unknown);
      setPublishError(unknown ? 'Não recebemos a confirmação. Confira Meus pedidos antes de enviar novamente; seu rascunho foi mantido.' : errorMessage(error));
    } finally { operation.current = false; setBusy(false); }
  }
  function accountControls() {
    if (sessionLoading) return <span role="status">Verificando conta…</span>;
    if (!session) return <button className="th-button secondary" onClick={() => { setAuthProfessional(false); setAuthOpen(true); }}>Entrar</button>;
    return <><span className="th-account-name" title={session.nome}>Olá, {session.nome}</span>{session.idCliente && session.perfis.includes('CLIENTE') && <button className="th-nav-link" disabled={busy} onClick={() => navigate('requests')}>Meus pedidos</button>}{session.idTecnico && session.perfis.includes('TECNICO') && <button className="th-nav-link" disabled={busy} onClick={() => navigate('technician')}>Área do técnico</button>}<button disabled={busy} className="th-button secondary" onClick={logout}>Sair</button></>;
  }
  function navLinks() { return navigation.map(([id, label]) => <button key={id} className={`th-nav-link${id === 'ferramentas' ? ' th-nav-rental' : ''}`} onClick={() => sectionLink(id)}>{id === 'ferramentas' && <Wrench size={15} />}{label}</button>); }
  function catalogOptions() {
    if (catalog.status === 'loading') return <p role="status">Carregando áreas de atendimento…</p>;
    if (catalog.status === 'error') return <div role="alert"><p>Não conseguimos carregar as áreas de atendimento.</p><button className="th-button secondary" onClick={retry}>Tentar novamente</button></div>;
    if (!catalog.categories.length) return <p role="status">Nenhuma área de atendimento está disponível neste momento.</p>;
    return <div className="th-options">{catalog.categories.filter(c => areaFor(c.nome)).map(c => <button className="th-option" key={c.idCategoria} onClick={() => choose(c)}><span>{c.nome}<small>{c.descricao}</small></span><ArrowRight size={18} /></button>)}<button className="th-option" onClick={() => navigate('unsure')}><CircleHelp size={22} />Não sei por onde começar</button></div>;
  }

  return <div className={`techhelp-site${paused ? ' th-paused th-motion-paused' : ''}`}>
    <a className="th-skip" href="#conteudo">Ir para o conteúdo</a>
    <header className="th-header"><div className="th-inner th-header-inner"><button className="th-brand" onClick={() => navigate('home')} aria-label="TechHelp início"><svg className="th-mark" viewBox="0 0 26 26" aria-hidden="true"><rect className="th-mark-body" x="1" y="1" width="24" height="24" rx="7" strokeWidth="1.5" /><path className="th-mark-line" d="M6.5 18 12 12.5 15.5 16" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" /><circle className="th-mark-signal" cx="18.5" cy="8.5" r="3" /></svg>TechHelp</button>
      <nav className="th-nav" aria-label="Navegação principal">{navLinks()}{accountControls()}</nav><button className="th-button secondary th-menu-toggle" aria-expanded={menu} aria-controls="menu-mobile" onClick={() => setMenu(!menu)}>{menu ? <X size={18} /> : <Menu size={18} />}{menu ? 'Fechar' : 'Menu'}</button>
    </div></header>
    <nav id="menu-mobile" className="th-mobile-nav" aria-label="Menu mobile" hidden={!menu}><div className="th-inner">{navLinks()}{accountControls()}</div></nav>
    <main id="conteudo" className={screen === 'home' ? undefined : 'th-app-content'} ref={main} tabIndex={-1}>
      {sessionError && <p role="status" className="th-note">{sessionError}</p>}
      {screen === 'request' && <RequestAssistant draft={draft} onChange={updateDraft} saved={saved} busy={busy || sessionLoading} blocked={uncertain} publishError={publishError} onCheckRequests={() => navigate('requests')} onHome={() => navigate('home')} onCategory={() => navigate('categories')} onPublish={publish} />}
      {screen === 'technician' && session?.idTecnico && <TechnicianBoard key={session.idTecnico} idTecnico={session.idTecnico} onBack={() => navigate('home')} />}
      {screen === 'requests' && session?.idCliente && <MyRequests key={session.idCliente} idCliente={session.idCliente} uncertain={uncertain} onBack={() => { setUncertain(false); setPublishError(''); navigate(draft.area ? 'request' : 'home'); }} />}
      {screen === 'published' && published && <section className="th-wizard th-flow-content" role="status"><h1 className="th-form-title">Solicitação publicada!</h1><p>Pedido #{published.idSolicitacao}: {published.titulo}</p><p>Seu pedido foi recebido. Consulte Meus pedidos para comparar as propostas quando chegarem.</p><div className="th-actions"><button className="th-button" onClick={() => navigate('requests')}>Ver meus pedidos</button><button className="th-button secondary" onClick={() => navigate('categories')}>Criar outro pedido</button></div></section>}
      {(screen === 'categories' || screen === 'unsure') && <section className="th-wizard"><button className="th-link" onClick={() => navigate('home')}>← Voltar à Home</button><div className="th-flow-content"><h1 className="th-form-title">{screen === 'unsure' ? 'Conte o que está acontecendo.' : 'Qual área parece mais próxima?'}</h1><p className="th-sub">Não precisa descobrir a causa. As jornadas disponíveis são Hardware, Redes e Software.</p>{screen === 'unsure' && <label className="th-label">Sua descrição<textarea className="th-field" maxLength={3000} value={draft.details} onChange={e => updateDraft({ ...draft, details: e.target.value })} placeholder="Ex.: meu notebook liga, mas a tela fica preta" /></label>}{catalogOptions()}</div></section>}
      {screen === 'home' && <Landing catalog={catalog} paused={paused} hasDraft={Boolean(draft.area || draft.details.trim())} onChoose={choose} onRetry={retry} onUnsure={() => navigate('unsure')} onStart={() => navigate('categories')} onResume={resume} onRental={() => notify('Aluguel de ferramentas', 'As ferramentas serão do próprio TechHelp. O catálogo, as reservas e a entrega ainda estão em preparação.')} onProfessional={() => { if (session?.idTecnico && session.perfis.includes('TECNICO')) navigate('technician'); else { setAuthProfessional(true); setAuthOpen(true); } }} />}
    </main>
    <footer className="th-footer"><div className="th-inner th-footer-inner"><div className="th-footer-brand"><strong>TechHelp</strong><span>Serviços de TI e aluguel de ferramentas técnicas.</span><span>Projeto Integrador de Software, SENAC Taboão da Serra.</span></div><div className="th-footer-links"><button onClick={() => sectionLink('como-funciona')}>Como funciona</button><button onClick={() => sectionLink('ferramentas')}>Aluguel de ferramentas</button><button onClick={() => sectionLink('profissionais')}>Sou profissional</button><button aria-pressed={paused} onClick={() => setPaused(!paused)}>{paused ? <Play size={14} /> : <Pause size={14} />}{paused ? 'Retomar movimento' : 'Pausar movimento'}</button></div></div></footer>
    <dialog className="th-dialog" ref={dialog} aria-labelledby="dialog-title" onClick={e => { if (e.target === e.currentTarget) dialog.current?.close(); }}><h2 id="dialog-title">{message.title}</h2><p>{message.body}</p><button className="th-button" onClick={() => dialog.current?.close()}>Entendi</button></dialog>
    {authOpen && <AuthDialog initialProfessional={authProfessional} onClose={() => { setAuthOpen(false); setAuthProfessional(false); }} onAuthenticated={value => { setAuthProfessional(false); setSession(value); setSessionError(''); setAuthOpen(false); setMenu(false); setPublishError(''); }} />}
  </div>;
}
