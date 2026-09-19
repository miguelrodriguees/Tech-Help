import { useEffect, useRef, useState, type ElementType } from 'react';
import { AppWindow, ArrowRight, ArrowUpRight, BadgeCheck, BookmarkCheck, BriefcaseBusiness, Cable, Check, CircleHelp, Cpu, Handshake, Images, Laptop, ListChecks, ListFilter, Menu, MessageSquare, MessageSquareText, Microchip, MousePointer2, Network, Pause, Play, Printer, Route, Server, Shield, Wrench, X } from 'lucide-react';
import { api } from '../services/api';
import type { Categoria } from '../types/Categoria';
import RequestAssistant from '../features/solicitacao/RequestAssistant';
import { areaFor, draftKey, emptyDraft, parseDraft, type Draft } from '../features/solicitacao/flow';

type Catalog = { status: 'loading' | 'ready' | 'error'; categories: Categoria[] };
type Screen = 'home' | 'categories' | 'unsure' | 'request';

function categoryIcon(name: string): ElementType {
  const value = name.toLowerCase();
  if (value.includes('hardware')) return Laptop;
  if (value.includes('rede')) return Network;
  if (value.includes('software')) return AppWindow;
  if (value.includes('segur')) return Shield;
  if (value.includes('impress') || value.includes('perif')) return Printer;
  return Server;
}

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

  useEffect(() => {
    const controller = new AbortController();
    api.get<Categoria[]>('/categorias', { signal: controller.signal }).then(response => {
      if (!Array.isArray(response.data) || response.data.some(c => !c || !Number.isSafeInteger(c.idCategoria) || c.idCategoria <= 0 || typeof c.nome !== 'string' || typeof c.ativo !== 'boolean')) throw new Error('Catálogo inválido');
      setCatalog({ status: 'ready', categories: response.data.filter(c => c.ativo) });
    }).catch(() => { if (!controller.signal.aborted) setCatalog({ status: 'error', categories: [] }); });
    return () => controller.abort();
  }, [reload]);

  useEffect(() => {
    if (screen !== 'home') main.current?.focus({ preventScroll: true });
    if (!('IntersectionObserver' in window) || paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const observer = new IntersectionObserver(entries => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue;
        entry.target.animate?.([{ opacity: 0.3, transform: 'translateY(18px)' }, { opacity: 1, transform: 'translateY(0)' }], { duration: 500, easing: 'cubic-bezier(.22,1,.36,1)' });
        observer.unobserve(entry.target);
      }
    }, { threshold: 0.08 });
    main.current?.querySelectorAll('.th-section').forEach(section => observer.observe(section));
    return () => observer.disconnect();
  }, [screen, paused]);

  function updateDraft(next: Draft) {
    setDraft(next);
    try { sessionStorage.setItem(draftKey, JSON.stringify(next)); setSaved(true); }
    catch { setSaved(false); }
  }

  function navigate(next: Screen) {
    setMenu(false);
    setScreen(next);
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  function notify(title: string, body: string) {
    setMessage({ title, body });
    dialog.current?.showModal();
  }

  function choose(category: Categoria) {
    const area = areaFor(category.nome);
    if (!area) { notify(category.nome, 'As perguntas desta área ainda estão em preparação. Por enquanto, o assistente está disponível para Hardware, Redes e Software.'); return; }
    updateDraft({ ...draft, categoryId: category.idCategoria, area, mode: draft.area === area ? draft.mode : '' });
    navigate('request');
  }

  function sectionLink(id: string) {
    setMenu(false);
    setScreen('home');
    requestAnimationFrame(() => document.getElementById(id)?.scrollIntoView({ behavior: paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' }));
  }

  function resume() {
    const category = catalog.categories.find(c => c.idCategoria === draft.categoryId && areaFor(c.nome) === draft.area);
    if (catalog.status !== 'ready' || !category) { navigate('categories'); notify('Confira a categoria do pedido', 'Precisamos de uma categoria disponível para continuar. Suas respostas anteriores permanecem guardadas.'); return; }
    navigate('request');
  }

  function catalogOptions(all = false) {
    if (catalog.status === 'loading') return <div role="status" aria-busy="true"><p className="th-sub">Carregando áreas de atendimento…</p><div className="th-skeleton" /><div className="th-skeleton" /></div>;
    if (catalog.status === 'error') return <div role="alert"><p className="th-sub">Não conseguimos carregar as áreas de atendimento. Tente novamente.</p><button className="th-button secondary" onClick={() => { setCatalog({ status: 'loading', categories: [] }); setReload(value => value + 1); }}>Tentar novamente</button></div>;
    if (!catalog.categories.length) return <p className="th-note" role="status">Nenhuma área de atendimento está disponível neste momento. Tente novamente mais tarde.</p>;
    const categories = all ? catalog.categories : catalog.categories.filter(c => areaFor(c.nome));
    return <div className={all ? 'th-services' : 'th-options'}>
      {categories.map(category => { const Icon = categoryIcon(category.nome); return <button type="button" className={all ? 'th-service' : 'th-option'} key={category.idCategoria} onClick={() => choose(category)}><Icon size={23} /><span>{category.nome}<small>{category.descricao}</small></span>{all && <ArrowUpRight size={18} />}</button>; })}
      {!all && <button type="button" className="th-option" onClick={() => navigate('unsure')}><CircleHelp size={23} /><span>Não sei por onde começar<small>Descreva com suas palavras</small></span></button>}
    </div>;
  }

  return <div className={`techhelp-site${paused ? ' th-motion-paused' : ''}`}>
    <a className="th-skip" href="#conteudo">Ir para o conteúdo</a>
    <div className="th-wrap">
      <header className="th-header">
        <button className="th-brand" onClick={() => navigate('home')} aria-label="TechHelp início">Tech<span>Help ↗</span></button>
        <nav className="th-nav" aria-label="Navegação principal">
          <button onClick={() => sectionLink('servicos')}>Serviços</button><button onClick={() => sectionLink('como-funciona')}>Como funciona</button><button onClick={() => sectionLink('profissionais')}>Sou profissional</button>
          <button className="th-button secondary" onClick={() => notify('Acesso à conta em preparação', 'O acesso à conta ainda não está disponível. Você pode começar seu pedido e guardar as respostas nesta aba.')}>Entrar</button>
        </nav>
        <button className="th-button secondary th-menu" aria-expanded={menu} aria-controls="menu-mobile" onClick={() => setMenu(!menu)}>{menu ? <X size={18} /> : <Menu size={18} />}{menu ? 'Fechar' : 'Menu'}</button>
      </header>
      <nav id="menu-mobile" className="th-mobile-nav" aria-label="Menu mobile" hidden={!menu}>
        <button onClick={() => sectionLink('servicos')}>Serviços</button><button onClick={() => sectionLink('como-funciona')}>Como funciona</button><button onClick={() => sectionLink('profissionais')}>Sou profissional</button><button onClick={() => sectionLink('ferramentas')}>Ferramentas e kits</button>
        <button onClick={() => notify('Acesso à conta em preparação', 'O acesso à conta ainda não está disponível. Suas respostas permanecem nesta aba.')}>Entrar</button>
      </nav>
      <main id="conteudo" ref={main} tabIndex={-1}>
        {screen === 'request' && <RequestAssistant draft={draft} onChange={updateDraft} saved={saved} onHome={() => navigate('home')} onCategory={() => navigate('categories')} onPublish={() => notify('Seu pedido ainda não foi publicado', 'A publicação será liberada junto com o acesso à conta. Suas respostas continuam no rascunho, sem envio ao servidor. Você pode voltar e revisar tudo.')} />}
        {(screen === 'categories' || screen === 'unsure') && <section className="th-wizard">
          <button className="th-link" onClick={() => navigate('home')}>← Voltar à Home</button>
          <div className="th-flow-content"><h1 className="th-form-title">{screen === 'unsure' ? 'Conte o que está acontecendo.' : 'Qual área parece mais próxima?'}</h1><p className="th-sub">Não precisa descobrir a causa. As jornadas disponíveis são Hardware, Redes e Software.</p>
            {screen === 'unsure' && <label className="th-label">Sua descrição<textarea className="th-field" maxLength={3000} value={draft.details} onChange={e => updateDraft({ ...draft, details: e.target.value })} placeholder="Ex.: meu notebook liga, mas a tela fica preta" /></label>}
            {catalogOptions()}
          </div>
        </section>}
        {screen === 'home' && <>
          <section className="th-hero">
            <div className="th-hero-copy"><p className="th-eyebrow">TI para a vida real</p><h1>Seu problema de TI.<br /><em>Um começo simples.</em></h1><p className="th-lead">Conte o que está acontecendo. Vamos ajudar você a organizar seu pedido de atendimento.</p><p className="th-footnote">Não precisa saber termos técnicos.<br />Você só entra na conta quando decidir publicar.</p>
              {draft.area && <button className="th-link" onClick={resume}><BookmarkCheck size={16} />Continuar minha solicitação</button>}
              <div className="th-device-scene" role="img" aria-label="Ilustração de notebook, hardware, rede e software">
                <div className="th-laptop-art"><div className="th-display-art"><Cpu /><div className="th-art-lines"><b /><b /><b /></div><span className="th-art-label">Seu universo de TI.</span></div></div>
                <div className="th-satellite network"><Network /><span>Redes</span></div><div className="th-satellite chip"><Microchip /><span>Hardware</span></div><div className="th-satellite code"><AppWindow /><span>Software</span></div>
              </div>
            </div>
            <div className="th-assistant"><p className="th-eyebrow"><Route size={18} />Comece por aqui</p><h2>Com o que você precisa de ajuda?</h2><p className="th-sub">Escolha a opção mais próxima do seu problema.</p>{catalogOptions()}</div>
          </section>
          <div className="th-band"><span><MousePointer2 />Escolhas simples, no seu ritmo.</span><span><ListChecks />Revise antes de publicar.</span><span><Handshake />Você decide com quem combinar.</span></div>
          <section className="th-section" id="servicos"><div className="th-section-head"><h2>Encontre o seu ponto de partida.</h2><p className="th-sub">Do notebook que não liga à rede que precisa melhorar. Estas são as áreas do TechHelp.</p></div>{catalogOptions(true)}</section>
          <section className="th-section th-how" id="como-funciona"><p className="th-eyebrow">Como será o atendimento</p><h2>Do problema ao serviço, com clareza.</h2><div className="th-steps">
            <div><span className="th-step-icon"><MessageSquareText /></span><span className="th-step-no">01 / Descreva</span><h3>Conte o que precisa.</h3><p className="th-sub">Perguntas simples ajudam a montar a solicitação.</p></div>
            <div><span className="th-step-icon"><ListFilter /></span><span className="th-step-no">02 / Compare</span><h3>Conheça as propostas.</h3><p className="th-sub">Quando chegarem, compare valores, condições e perfis.</p></div>
            <div><span className="th-step-icon"><Handshake /></span><span className="th-step-no">03 / Combine e avalie</span><h3>Acompanhe o atendimento.</h3><p className="th-sub">Combine os detalhes e avalie depois da conclusão.</p></div>
          </div></section>
          <section className="th-section th-split"><div><p className="th-eyebrow">Para quem precisa de ajuda</p><h2>Sem precisar traduzir seu problema para “tecnês”.</h2></div><ul className="th-benefits"><li><Check />“Meu notebook liga, mas a tela fica preta” já é um bom começo.</li><li><Check />Não sabe a marca ou a causa? Tudo bem.</li><li><Check />Leia e edite seu pedido antes de publicá-lo.</li></ul></section>
          <section className="th-section th-split" id="profissionais"><div><p className="th-eyebrow">Para profissionais</p><h2>Deixe sua experiência falar.</h2><p className="th-sub">Os perfis poderão reunir especialidades, portfólio, certificações e avaliações de serviços concluídos.</p>
            <div className="th-profile-map"><p>Estrutura de um perfil profissional</p><div><Wrench />Especialidades</div><div><Images />Portfólio de trabalhos</div><div><BadgeCheck />Certificações informadas</div><div><MessageSquare />Avaliações após o serviço</div></div>
            <details><summary>Como será a área profissional</summary><p className="th-sub">O profissional poderá montar seu perfil, consultar solicitações, enviar propostas e acompanhar seus serviços. A área profissional ainda não está disponível neste site.</p></details>
          </div><div className="th-kit" id="ferramentas"><div className="th-tools" aria-hidden="true"><span><BriefcaseBusiness /></span><span><Wrench /></span><span><Cable /></span></div><p className="th-eyebrow">Ferramentas e kits</p><h2>O equipamento também faz parte do trabalho.</h2><p className="th-sub">O TechHelp prevê a locação de kits de ferramentas para apoiar atendimentos técnicos.</p><details><summary>Entenda a locação</summary><p className="th-sub">A proposta é consultar o catálogo, reservar um kit e acompanhar retirada e devolução. O catálogo de locação ainda não está disponível neste site.</p></details></div></section>
          <section className="th-section th-final"><ArrowUpRight aria-hidden="true" /><h2>Vamos começar pelo que você já sabe.</h2><p className="th-sub">Descreva o problema. Os detalhes técnicos vêm depois.</p><button className="th-button" onClick={() => navigate('categories')}>Começar minha solicitação<ArrowRight size={17} /></button></section>
        </>}
      </main>
      <footer className="th-footer"><span>TechHelp · Serviços de TI</span><button onClick={() => sectionLink('como-funciona')}>Como funciona</button><button onClick={() => sectionLink('ferramentas')}>Ferramentas e kits</button><button aria-pressed={paused} onClick={() => setPaused(!paused)}>{paused ? <Play size={14} /> : <Pause size={14} />}{paused ? 'Retomar movimento' : 'Pausar movimento'}</button></footer>
    </div>
    <dialog className="th-dialog" ref={dialog} aria-labelledby="dialog-title" onClick={e => { if (e.target === e.currentTarget) dialog.current?.close(); }}><h2 id="dialog-title">{message.title}</h2><p>{message.body}</p><button className="th-button" onClick={() => dialog.current?.close()}>Entendi</button></dialog>
  </div>;
}
