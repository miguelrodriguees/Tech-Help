import { useEffect, useRef } from 'react';

const steps = [
 ['Escolha o serviço', 'Selecione a área e conte o que precisa resolver.'],
 ['Escolha o profissional', 'Compare as propostas e combine a melhor data para o atendimento.'],
 ['Receba o atendimento', 'O profissional realiza o serviço presencialmente ou a distância, conforme combinado.'],
 ['Confirme a conclusão', 'Confira o serviço realizado e avalie sua experiência.'],
];

export default function NarrativeSection({ paused }: { paused: boolean }) {
 const ref = useRef<HTMLElement>(null);
 useEffect(() => {
  const section = ref.current;
  if (!section) return;
  const track = section.querySelector<HTMLElement>('.journey-steps')!;
  const articles = [...track.querySelectorAll<HTMLElement>('article')];
  const lines = [...track.querySelectorAll<SVGLineElement>('line')];
  const star = track.querySelector<SVGTextElement>('text')!;
  const media = matchMedia('(prefers-reduced-motion: reduce)');
  let frame = 0, celebrated = false;
  function update() {
   frame = 0;
   if (!section) return;
   const still = paused || media.matches;
   section.classList.toggle('journey-static', still);
   const header = document.querySelector('header')?.getBoundingClientRect().height ?? 76;
   const distance = Math.max(1, section.offsetHeight - (innerHeight - header));
   const progress = Math.max(0, Math.min(1, (header - section.getBoundingClientRect().top) / distance));
   const travel = Math.min(3, progress / .24);
   const bounds = track.getBoundingClientRect();
   const numbers = articles.map(el => el.querySelector('strong')!.getBoundingClientRect());
   const vertical = matchMedia('(max-width:700px)').matches;
   articles.forEach((el, i) => {
    const visible = still || i <= Math.floor(travel);
    el.classList.toggle('revealed', visible);
    el.setAttribute('aria-hidden', String(!visible));
   });
   numbers.slice(0,3).forEach((r,i) => {
    const next = numbers[i+1];
    const x1 = vertical ? r.left-bounds.left+r.width/2 : r.right-bounds.left+18;
    const y1 = vertical ? r.bottom-bounds.top+8 : r.top-bounds.top+r.height/2;
    const x2 = vertical ? next.left-bounds.left+next.width/2 : next.left-bounds.left-18;
    const y2 = vertical ? next.top-bounds.top-8 : next.top-bounds.top+next.height/2;
    const amount = still ? 1 : Math.max(0,Math.min(1,travel-i));
    const x = x1+(x2-x1)*amount, y = y1+(y2-y1)*amount;
    const line = lines[i];
    for (const [name,value] of Object.entries({x1,y1,x2:x,y2:y})) line.setAttribute(name,String(value));
    line.style.opacity = amount > 0 ? '1' : '0';
    if(i === Math.min(2,Math.floor(travel))) star.setAttribute('transform',`translate(${x} ${y}) rotate(${travel*180})`);
   });
   star.style.opacity = still || travel===0 ? '0' : '1';
   section.querySelector('.journey-hint')!.textContent = travel===3 ? 'Etapas completas. Continue para conhecer as ferramentas ↓' : 'Role para acompanhar o próximo passo ↓';
   if(travel===3 && !celebrated && !still) {
    celebrated=true;
    section.querySelectorAll<HTMLElement>('.journey-confetti i').forEach((piece,i) => {
     piece.getAnimations().forEach(animation=>animation.cancel());
     piece.animate([{opacity:0,transform:'translateY(35px) rotate(0)'},{opacity:1,offset:.15},{opacity:0,transform:`translateY(-190px) rotate(${i*39}deg)`}],{duration:1500,delay:(i%6)*50,easing:'ease-out',fill:'both'});
    });
   }
   if(progress<.6) celebrated=false;
  }
  const schedule = () => { if(!frame) frame=requestAnimationFrame(update); };
  const resize = new ResizeObserver(schedule);
  resize.observe(track);
  addEventListener('scroll',schedule,{passive:true});
  addEventListener('resize',schedule);
  media.addEventListener('change',schedule);
  update();
  return () => { cancelAnimationFrame(frame); resize.disconnect(); removeEventListener('scroll',schedule);removeEventListener('resize',schedule);media.removeEventListener('change',schedule); };
 },[paused]);
 return <section ref={ref} className="journey" id="como-funciona" aria-labelledby="journey-title"><div className="journey-pin"><div className="th-inner">
  <h2 id="journey-title">Como funciona?</h2>
  <div className="journey-steps"><svg className="journey-scroll-art" aria-hidden="true"><g className="journey-scroll-lines">{[0,1,2].map(i=><line key={i}/>)}</g><text className="journey-scroll-star" textAnchor="middle" dominantBaseline="central">✦</text></svg>
  {steps.map(([title,text],i)=><article key={title} className={`journey-step${i===0?' revealed':''}`}><strong aria-hidden="true">0{i+1}</strong><h3>{title}</h3><p>{text}</p></article>)}</div>
  <p className="journey-hint">Role para acompanhar o próximo passo ↓</p>
  <div className="journey-confetti" aria-hidden="true">{Array.from({length:30},(_,i)=><i key={i} style={{left:`${i*37%100}%`,animation:'none',opacity:0}} />)}</div>
 </div></div></section>;
}
