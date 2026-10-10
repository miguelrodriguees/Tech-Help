import { useEffect, useRef, useState } from 'react';
import { AppWindow, ArrowRight, BookmarkCheck, CircleHelp, Laptop, Network, Printer, Server, Shield } from 'lucide-react';
import type { Categoria } from '../../types/Categoria';
import { areaFor } from '../solicitacao/flow';
import { createModuleField } from './moduleField';
import { diagramFor, diagramKeyFor, rentalArt } from './illustrations';

interface Props {
  catalog: { status: 'loading' | 'ready' | 'error'; categories: Categoria[] };
  paused: boolean;
  hasDraft: boolean;
  onChoose(category: Categoria): void;
  onRetry(): void;
  onUnsure(): void;
  onStart(): void;
  onResume(): void;
  onProfessional(): void;
  onRental(): void;
}

const steps = [
  ['Escolha o serviço', 'Selecione a área e conte o que precisa resolver.'],
  ['Escolha o profissional', 'Compare as propostas e combine a melhor data para o atendimento.'],
  ['Receba o atendimento', 'O profissional realiza o serviço presencialmente ou a distância, conforme combinado.'],
  ['Confirme a conclusão', 'Confira o serviço realizado e avalie sua experiência.'],
];

function AreaIcon({ name }: { name: string }) {
  const key = diagramKeyFor(name);
  const Icon = ({ hardware: Laptop, redes: Network, software: AppWindow, seguranca: Shield, servidores: Server, perifericos: Printer })[key] ?? Server;
  return <Icon size={22} />;
}

function Journey({ paused }: { paused: boolean }) {
  const root = useRef<HTMLElement>(null);
  useEffect(() => {
    const section = root.current;
    if (!section) return;
    const media = matchMedia('(prefers-reduced-motion: reduce)');
    let frame = 0;
    let celebrated = false;
    let celebrations: Animation[] = [];
    const update = () => {
      frame = 0;
      const still = paused || media.matches;
      if (still) { celebrations.forEach(animation => animation.cancel()); celebrations = []; }
      section.classList.toggle('journey-static', still);
      const header = document.querySelector('.th-header')?.getBoundingClientRect().height ?? 76;
      const progress = Math.max(0, Math.min(1, (header - section.getBoundingClientRect().top) / Math.max(1, section.offsetHeight - (innerHeight - header))));
      const step = Math.min(3, Math.floor(progress / .24));
      section.querySelectorAll<HTMLElement>('.journey-step').forEach((element, index) => {
        element.classList.toggle('revealed', still || index <= step);
        // Keep the complete explanation available to assistive technology.
      });
      const track = section.querySelector('.journey-steps')!;
      const bounds = track.getBoundingClientRect();
      const numbers = [...track.querySelectorAll('strong')].map(element => element.getBoundingClientRect());
      const vertical = matchMedia('(max-width:700px)').matches;
      const travel = Math.min(3, progress / .24);
      const segments = numbers.slice(0, 3).map((rect, index) => {
        const next = numbers[index + 1];
        return vertical
          ? { x1: rect.left - bounds.left + rect.width / 2, y1: rect.bottom - bounds.top + 8, x2: next.left - bounds.left + next.width / 2, y2: next.top - bounds.top - 8 }
          : { x1: rect.right - bounds.left + 18, y1: rect.top - bounds.top + rect.height / 2, x2: next.left - bounds.left - 18, y2: next.top - bounds.top + next.height / 2 };
      });
      section.querySelectorAll('line').forEach((line, index) => {
        const g = segments[index];
        const t = still ? 1 : Math.max(0, Math.min(1, travel - index));
        Object.entries({ x1: g.x1, y1: g.y1, x2: g.x1 + (g.x2 - g.x1) * t, y2: g.y1 + (g.y2 - g.y1) * t, opacity: t > 0 ? 1 : 0 }).forEach(([key, value]) => line.setAttribute(key, String(value)));
      });
      const segment = Math.min(2, Math.floor(travel));
      const g = segments[segment];
      const t = Math.min(1, travel - segment);
      const star = section.querySelector<SVGTextElement>('.journey-scroll-star')!;
      star.setAttribute('transform', `translate(${g.x1 + (g.x2 - g.x1) * t} ${g.y1 + (g.y2 - g.y1) * t}) rotate(${travel * 180})`);
      star.style.opacity = still || travel === 0 ? '0' : '1';
      section.querySelector('.journey-hint')!.textContent = still ? 'Do pedido à avaliação, uma etapa por vez.' : step === 3 ? 'Etapas completas. Continue para conhecer as ferramentas ↓' : 'Role para acompanhar o próximo passo ↓';
      if (step === 3 && !celebrated && !still) {
        celebrated = true;
        celebrations.forEach(animation => animation.cancel());
        celebrations = [...section.querySelectorAll<HTMLElement>('.journey-confetti i')].map(element => element.animate([{ opacity: 0, transform: 'translateY(35px)' }, { opacity: 1, offset: .15 }, { opacity: 0, transform: 'translateY(-190px) rotate(180deg)' }], { duration: 1500 }));
      }
      if (progress < .6) celebrated = false;
    };
    const schedule = () => { if (!frame) frame = requestAnimationFrame(update); };
    addEventListener('scroll', schedule, { passive: true });
    addEventListener('resize', schedule);
    media.addEventListener('change', schedule);
    update();
    return () => { celebrations.forEach(animation => animation.cancel()); cancelAnimationFrame(frame); removeEventListener('scroll', schedule); removeEventListener('resize', schedule); media.removeEventListener('change', schedule); };
  }, [paused]);
  return <section ref={root} className="journey" id="como-funciona" aria-labelledby="journey-title"><div className="journey-pin"><div className="th-inner">
    <h2 id="journey-title">Como funciona?</h2>
    <div className="journey-steps"><svg className="journey-scroll-art" aria-hidden="true"><g className="journey-scroll-lines">{[0, 1, 2].map(n => <line key={n} />)}</g><text className="journey-scroll-star" textAnchor="middle" dominantBaseline="central">✦</text></svg>{steps.map(([title, text], index) => <article className={`journey-step${index === 0 ? ' revealed' : ''}`} key={title}><strong aria-hidden="true">0{index + 1}</strong><h3>{title}</h3><p>{text}</p></article>)}</div>
    <p className="journey-hint">Role para acompanhar o próximo passo ↓</p><div className="journey-confetti" aria-hidden="true">{Array.from({ length: 30 }, (_, index) => <i key={index} style={{ left: `${index * 37 % 100}%`, animation: 'none' }} />)}</div>
  </div></div></section>;
}

export default function Landing(props: Props) {
  const { catalog, paused, hasDraft, onChoose, onRetry } = props;
  const canvas = useRef<HTMLCanvasElement>(null);
  const [active, setActive] = useState<number | null>(null);
  const current = catalog.categories.find(c => c.idCategoria === active) ?? catalog.categories[0];
  useEffect(() => {
    if (!canvas.current) return;
    const media = matchMedia('(prefers-reduced-motion: reduce)');
    const field = createModuleField(canvas.current, { still: paused || media.matches });
    const change = () => field.setStill(paused || media.matches);
    media.addEventListener('change', change);
    return () => { media.removeEventListener('change', change); field.destroy(); };
  }, [paused]);
  function unavailable() {
    if (catalog.status === 'loading') return <div className="hx-state" role="status" aria-busy="true">Carregando áreas de atendimento…{[0, 1, 2].map(n => <div key={n} className="hx-skeleton" />)}</div>;
    return <div className="hx-state" role={catalog.status === 'error' ? 'alert' : 'status'}><p><strong>{catalog.status === 'error' ? 'As áreas não carregaram.' : 'Nenhuma área está ativa agora.'}</strong> {catalog.status === 'error' ? 'Não foi possível consultar o servidor do TechHelp.' : 'Consulte novamente mais tarde.'}</p><button className="th-button secondary" onClick={onRetry}>{catalog.status === 'error' ? 'Tentar novamente' : 'Atualizar'}</button></div>;
  }
  const ready = catalog.categories.filter(c => areaFor(c.nome));
  return <>
    <section className="hx-stage" aria-labelledby="hero-title"><div className="hx-canvas-wrap" aria-hidden="true"><canvas ref={canvas} /></div><div className="hx-scrim" aria-hidden="true" /><div className="th-inner hx-stage-inner">
      <h1 className="th-display hx-title th-enter th-enter-1" id="hero-title">Seu problema em TI.<br /><span className="hx-soft">Um começo simples.</span></h1>
      <p className="th-lead th-enter th-enter-2">Notebook, programas ou conexão: conte o que aconteceu. O TechHelp ajuda você a organizar o pedido e encontrar atendimento.</p>
      {hasDraft && <button className="th-link" onClick={props.onResume}><BookmarkCheck size={16} />Continuar de onde parei</button>}
      <div className="hx-command th-enter th-enter-3"><div className="hx-command-label"><b>Descrever meu problema</b><span>Comece sem criar uma conta.</span></div>
        {catalog.status !== 'ready' || !ready.length ? unavailable() : <div className="hx-chips">{ready.map(c => <button className="hx-chip-btn" key={c.idCategoria} onClick={() => onChoose(c)}><AreaIcon name={c.nome} />{c.nome}</button>)}<button className="hx-chip-btn ghost" onClick={props.onUnsure}><CircleHelp size={22} />Não sei dizer</button></div>}
        <p className="hx-command-note">Responda no seu ritmo. A conta entra apenas na publicação.</p>
      </div>
    </div></section>
    <section className="hx-explorer" id="servicos" aria-labelledby="servicos-title"><div className="th-inner"><div className="hx-explorer-head"><h2 className="th-h2" id="servicos-title">Qual parte precisa de ajuda?</h2><p className="th-body">Passe pelas áreas para conhecer os serviços. Escolha uma para começar pelas perguntas mais simples.</p></div>
      {catalog.status !== 'ready' || !current ? unavailable() : <div className="hx-explorer-grid"><div className="hx-list">{catalog.categories.map(c => <button className={`hx-row${current.idCategoria === c.idCategoria ? ' is-active' : ''}`} key={c.idCategoria} onMouseEnter={() => setActive(c.idCategoria)} onFocus={() => setActive(c.idCategoria)} onClick={() => onChoose(c)}><AreaIcon name={c.nome} /><span className="hx-row-name">{c.nome}</span><span className={`th-chip${areaFor(c.nome) ? ' live' : ''}`}>{areaFor(c.nome) ? 'Perguntas prontas' : 'Em preparação'}</span><span className="hx-row-desc">{c.descricao || 'Sem descrição cadastrada.'}</span></button>)}</div><figure className="hx-figure"><div dangerouslySetInnerHTML={{ __html: diagramFor(diagramKeyFor(current.nome)) }} /><figcaption><b>{current.nome}</b>{current.descricao}</figcaption></figure></div>}
    </div></section>
    <Journey paused={paused} />
    <section className="rent-v2" id="ferramentas" aria-labelledby="aluguel-title"><div className="th-inner rent-layout"><div><span className="section-label">FERRAMENTAS E KITS</span><h2 id="aluguel-title">Alugue a ferramenta.<br />Faça o trabalho.</h2><p>Equipamentos para manutenção de computadores e instalação de redes, pelo período que você precisar.</p><p className="rent-explanation">Escolha a ferramenta ou kit, combine o período e a retirada. Ao terminar, devolva o equipamento.</p><button className="th-button secondary" onClick={props.onRental}><span>Conhecer o aluguel</span><ArrowRight size={20} /></button><small>Catálogo e reservas em preparação.</small></div><figure className="rent-art"><div dangerouslySetInnerHTML={{ __html: rentalArt() }} /><figcaption><span>Testador de cabos de rede</span><span>Kit de chaves de precisão</span></figcaption><small>Ilustrações dos tipos de equipamento previstos.</small></figure></div></section>
    <section className="pro-v2" id="profissionais" aria-labelledby="pro-title"><div className="th-inner pro-layout"><div className="pro-invite"><span className="section-label">PARA PROFISSIONAIS DE TI</span><h2 id="pro-title">Você sabe resolver.<br />Mostre seu trabalho.</h2><p>Faça parte do TechHelp para apresentar suas especialidades e encontrar clientes que precisam do que você faz.</p><button className="th-button" onClick={props.onProfessional}><span>Quero ser profissional</span><ArrowRight size={20} /></button><small>Cadastre-se ou entre para consultar oportunidades e enviar propostas.</small></div><div className="pro-benefits"><div><span>01</span><h3>Apresente seu perfil</h3><p>Suas especialidades, experiências e portfólio em um só lugar. Perfil completo em preparação.</p></div><div><span>02</span><h3>Encontre solicitações</h3><p>Conheça o problema do cliente antes de enviar sua proposta.</p></div><div><span>03</span><h3>Combine o atendimento</h3><p>Acerte as condições com o cliente. Acompanhamento pela interface em preparação.</p></div></div></div></section>
    <section className="hx-end" aria-labelledby="fechar-title"><div className="th-inner hx-end-inner"><div className="hx-end-strip" aria-hidden="true">{Array.from({ length: 11 }, (_, index) => <i key={index} />)}</div><h2 className="th-h2" id="fechar-title">{hasDraft ? 'Seu pedido está esperando você' : 'Comece pelo que você já sabe'}</h2><p>{hasDraft ? 'As respostas continuam guardadas nesta aba. Dá para revisar e mudar tudo antes de publicar.' : 'Uma frase comum basta. O resto das perguntas é com a gente.'}</p><button className="th-button" onClick={hasDraft ? props.onResume : props.onStart}>{hasDraft ? 'Retomar meu pedido' : 'Descrever meu problema'}</button></div></section>
  </>;
}
