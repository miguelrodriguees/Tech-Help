import TechWorkspace from '../features/tecnico/TechWorkspace';
import MyRequests from '../features/solicitacao/MyRequests';
import { isAxiosError } from 'axios';
import { csrfHeaders } from '../features/auth/session';
import { toSolicitacaoPayload } from '../features/solicitacao/payload';
import AccountDialog from '../features/auth/AccountDialog';
import { sair, type Conta } from '../features/auth/session';
import { useEffect, useRef, useState } from 'react';
import { ArrowLeft } from 'lucide-react';
import { api } from '../services/api.ts';
import type { Categoria } from '../types/Categoria.ts';
import { areaFor, draftKey, emptyDraft, parseDraft, type Draft } from '../features/solicitacao/flow.ts';
import RequestAssistant from '../features/solicitacao/RequestAssistant.tsx';
import AreaChips from '../features/home/AreaChips.tsx';
import AreaExplorer from '../features/home/AreaExplorer.tsx';
import ClosingSection from '../features/home/ClosingSection.tsx';
import HeroStage from '../features/home/HeroStage.tsx';
import NarrativeSection from '../features/home/NarrativeSection.tsx';
import NoticeDialog, { type Notice } from '../features/home/NoticeDialog.tsx';
import ProfessionalsSection from '../features/home/ProfessionalsSection.tsx';
import RentalSection from '../features/home/RentalSection.tsx';
import SiteFooter from '../features/home/SiteFooter.tsx';
import SiteHeader from '../features/home/SiteHeader.tsx';
import { parseCatalog, type CatalogState } from '../features/home/catalog.ts';

type Screen = 'home' | 'categories' | 'unsure' | 'request' | 'requests' | 'technician';

function loadDraft(): Draft {
  try { return parseDraft(sessionStorage.getItem(draftKey)); }
  catch { return emptyDraft(); }
}

export default function Home() {
  const [publishing,setPublishing] = useState(false);
  const publishingLock = useRef(false);
  const [publishError,setPublishError] = useState('');
  const [published,setPublished] = useState<{idSolicitacao:number;titulo:string}|null>(null);
  const sendKey = useRef<{json:string;key:string}|null>(null);
  const [professionalIntent,setProfessionalIntent] = useState(false);
  const [accountOpen,setAccountOpen] = useState(false);
  const [conta,setConta] = useState<Conta|null>(null);
  useEffect(()=>{const controller=new AbortController();api.get<Conta>('/auth/me',{signal:controller.signal}).then(r=>setConta(r.data)).catch(()=>{});return()=>controller.abort();},[]);
  const [catalog, setCatalog] = useState<CatalogState>({ status: 'loading', categories: [] });
  const [reload, setReload] = useState(0);
  const [draft, setDraft] = useState<Draft>(loadDraft);
  const [saved, setSaved] = useState(() => {
    try { return sessionStorage.getItem(draftKey) !== null; } catch { return false; }
  });
  const [screen, setScreen] = useState<Screen>('home');
  const [menuOpen, setMenuOpen] = useState(false);
  const [paused, setPaused] = useState(false);
  const [notice, setNotice] = useState<Notice | null>(null);
  const main = useRef<HTMLElement>(null);

  useEffect(() => {
    const controller = new AbortController();
    api.get<unknown>('/categorias', { signal: controller.signal })
      .then(response => setCatalog({ status: 'ready', categories: parseCatalog(response.data) }))
      .catch(() => { if (!controller.signal.aborted) setCatalog({ status: 'error', categories: [] }); });
    return () => controller.abort();
  }, [reload]);

  useEffect(() => {
    if (screen !== 'home') main.current?.focus({ preventScroll: true });
  }, [screen]);

  function retry() {
    setCatalog({ status: 'loading', categories: [] });
    setReload(value => value + 1);
  }

  function updateDraft(next: Draft) {
    setDraft(next);
    try { sessionStorage.setItem(draftKey, JSON.stringify(next)); setSaved(true); }
    catch { setSaved(false); }
  }

  function navigate(next: Screen) {
    setMenuOpen(false);
    setScreen(next);
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  function goToSection(id: string) {
    setMenuOpen(false);
    setScreen('home');
    const reduce = paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    requestAnimationFrame(() => {
      document.getElementById(id)?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth' });
    });
  }

  function choose(category: Categoria) {
    setPublished(null);setPublishError('');
    const area = areaFor(category.nome);
    if (!area) {
      setNotice({
        title: `${category.nome} ainda não tem o assistente`,
        body: 'As perguntas dessa área estão em preparação. Por enquanto o assistente atende Hardware, Redes e Software. Se o seu caso encostar em uma dessas, dá para montar o pedido agora.',
      });
      return;
    }
    updateDraft({ ...draft, categoryId: category.idCategoria, area, mode: draft.area === area ? draft.mode : '' });
    navigate('request');
  }

  function resume() {
    const category = catalog.categories.find(
      item => item.idCategoria === draft.categoryId && areaFor(item.nome) === draft.area,
    );
    if (catalog.status !== 'ready' || !category) {
      navigate('categories');
      setNotice({
        title: 'Confirme a área do seu pedido',
        body: 'Precisamos de uma área disponível no servidor para continuar. Suas respostas anteriores continuam guardadas.',
      });
      return;
    }
    navigate('request');
  }

  async function publicar() {
    if(publishingLock.current)return;
    setPublishError('');
    if(!conta){setAccountOpen(true);return;}
    if(!conta.idCliente){setPublishError('Entre com uma conta de cliente para publicar.');return;}
    const result=toSolicitacaoPayload(draft);
    if(!result.ok){setPublishError(result.reason);return;}
    const json=JSON.stringify(result.payload);
    if(sendKey.current?.json!==json)sendKey.current={json,key:crypto.randomUUID()};
    publishingLock.current=true;setPublishing(true);
    try {
      const headers=await csrfHeaders();
      const {data}=await api.post<{idSolicitacao:number;titulo:string}>('/solicitacoes',result.payload,{headers:{...headers,'Idempotency-Key':sendKey.current!.key}});
      if(!Number.isSafeInteger(data.idSolicitacao)||data.idSolicitacao<=0)throw new Error('Resposta inválida');
      setPublished(data);setDraft(emptyDraft());
      try{sessionStorage.removeItem(draftKey);}catch{/* O pedido já foi confirmado pelo servidor. */}
      setSaved(false);
    } catch(err) {
      if(isAxiosError(err)&&err.response?.status===401){setConta(null);setAccountOpen(true);setPublishError('Sua sessão expirou. Entre novamente e confirme a publicação.');}
      else setPublishError(isAxiosError(err)&&typeof err.response?.data?.erro==='string'?err.response.data.erro:'Não foi possível confirmar a publicação. Suas respostas foram preservadas. Tente novamente nesta sessão.');
    } finally{publishingLock.current=false;setPublishing(false);}
  }

  const hasDraft = Boolean(draft.area) || Boolean(draft.details.trim());

  return (
    <div className={paused ? 'techhelp-site th-paused' : 'techhelp-site'}>
      <a className="th-skip" href="#conteudo">Ir para o conteúdo</a>

      <SiteHeader
        menuOpen={menuOpen}
        onToggleMenu={setMenuOpen}
        onBrand={() => navigate('home')}
        onSection={goToSection}
        onSignIn={() => {setProfessionalIntent(false);setAccountOpen(true);}}
      />

      {accountOpen && <AccountDialog professional={professionalIntent} onClose={()=>setAccountOpen(false)} onSuccess={account=>{setConta(account);setAccountOpen(false);if((professionalIntent||screen==='technician')&&account.idTecnico)navigate('technician');else if(screen==='requests'||screen==='technician')navigate('home');}} />}
      {conta && <div className="th-inner th-session"><span>Olá, {conta.nome}</span>{conta.idTecnico && <button className="th-link" onClick={()=>navigate('technician')}>Área profissional</button>}{conta.idCliente && <button className="th-link" onClick={()=>navigate('requests')}>Minhas solicitações</button>}<button className="th-link" onClick={()=>{sair().then(()=>{setConta(null);setPublished(null);navigate('home');}).catch(()=>setNotice({title:'Não foi possível sair',body:'Tente novamente. Sua sessão ainda pode estar ativa.'}));}}>Sair</button></div>}
      <main id="conteudo" ref={main} tabIndex={-1}>
        {screen === 'technician' && conta?.idTecnico && <TechWorkspace key={conta.idUsuario} onHome={()=>navigate('home')} onLogin={()=>{setProfessionalIntent(false);setAccountOpen(true);}} />}
        {screen === 'requests' && conta?.idCliente && <MyRequests key={conta.idUsuario} onHome={()=>navigate('home')} onCreate={()=>{setPublished(null);navigate('categories');}} onLogin={()=>setAccountOpen(true)} />}
        {screen === 'request' && published && <section className="th-wizard" role="status"><h1>Solicitação publicada</h1><p>Pedido #{published.idSolicitacao}: {published.titulo}</p><p>Seu pedido foi salvo. Acompanhe as propostas em Minhas solicitações.</p><button className="th-link" onClick={()=>navigate('requests')}>Ver minhas solicitações</button><button className="th-button" onClick={()=>{setPublished(null);sendKey.current=null;navigate('categories');}}>Criar outra solicitação</button><button className="th-link" onClick={()=>navigate('home')}>Voltar à Home</button></section>}
        {screen === 'request' && !published && (
          <RequestAssistant
            draft={draft}
            onChange={updateDraft}
            saved={saved}
            onHome={() => navigate('home')}
            onCategory={() => navigate('categories')}
            onPublish={publicar}
            publishing={publishing}
            publishError={publishError}
          />
        )}

        {(screen === 'categories' || screen === 'unsure') && (
          <section className="th-wizard" aria-labelledby="escolha-titulo">
            <div className="th-wizard-inner">
              <div className="th-wizard-top">
                <button type="button" className="th-link quiet" onClick={() => navigate('home')}>
                  <ArrowLeft aria-hidden="true" size={15} />
                  Voltar ao início
                </button>
              </div>
              <div className="th-panel th-flow">
                <div className="th-flow-head">
                  <h1 className="th-flow-title" id="escolha-titulo">
                    {screen === 'unsure' ? 'Conte o que está acontecendo' : 'Qual área chega mais perto?'}
                  </h1>
                  <p>
                    {screen === 'unsure'
                      ? 'Escreva com suas palavras. Depois escolha a área mais próxima e o assistente ajusta as perguntas.'
                      : 'Não precisa acertar a causa. O assistente já atende Hardware, Redes e Software.'}
                  </p>
                </div>

                {screen === 'unsure' && (
                  <>
                    <label className="th-label">
                      Sua descrição
                      <textarea
                        className="th-field"
                        maxLength={3000}
                        value={draft.details}
                        onChange={event => updateDraft({ ...draft, details: event.target.value })}
                        placeholder="Ex.: meu notebook liga, mas a tela fica preta"
                      />
                    </label>
                    <p className="th-field-meta">
                      <span>Este texto vai para a etapa de detalhes do pedido.</span>
                      <span>{draft.details.length} de 3000</span>
                    </p>
                  </>
                )}

                <div className="th-subgroup">
                  <h2>Áreas com perguntas prontas</h2>
                  <AreaChips
                    catalog={catalog}
                    onChoose={choose}
                    onRetry={retry}
                    onUnsure={screen === 'categories' ? () => navigate('unsure') : undefined}
                  />
                </div>
              </div>
            </div>
          </section>
        )}

        {screen === 'home' && (
          <>
            <HeroStage
              catalog={catalog}
              hasDraft={hasDraft}
              paused={paused}
              onChoose={choose}
              onRetry={retry}
              onUnsure={() => navigate('unsure')}
              onResume={resume}
            />
            <AreaExplorer catalog={catalog} onChoose={choose} onRetry={retry} />
            <NarrativeSection paused={paused} />
            <RentalSection onAction={() => setNotice({ title: 'Aluguel de ferramentas', body: 'O catálogo e as reservas estão em preparação. Você poderá escolher o equipamento, o período e combinar retirada e devolução.' })} />
            <ProfessionalsSection onAction={() => {if(conta?.idTecnico)navigate('technician');else if(conta)setNotice({title:'Seu perfil é de cliente',body:'A inclusão de um perfil profissional na mesma conta ainda está em preparação.'});else{setProfessionalIntent(true);setAccountOpen(true);}}} />
            <ClosingSection
              hasDraft={hasDraft}
              onStart={() => (hasDraft ? resume() : navigate('categories'))}
            />
          </>
        )}
      </main>

      <SiteFooter
        paused={paused}
        onTogglePaused={() => setPaused(value => !value)}
        onSection={goToSection}
      />

      <NoticeDialog notice={notice} onClose={() => setNotice(null)} />
    </div>
  );
}
