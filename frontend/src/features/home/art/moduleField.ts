/**
 * Campo de módulos: uma placa desenhada como centenas de peças quadradas.
 * Perto do ponteiro as peças se afastam, giram e perdem opacidade; ao sair,
 * voltam ao lugar por mola. Na carga, elas entram espalhadas e se juntam.
 *
 * Canvas 2D puro, sem biblioteca. A imagem é gerada a partir de uma máscara
 * desenhada fora da tela, então a forma é autoral e não depende de asset.
 *
 * É melhoria opcional: se algo falhar, o chamador continua com a página
 * inteira funcionando — nenhum conteúdo ou controle vive aqui dentro.
 */

export interface FieldOptions {
  /** Sem movimento: desenha a placa formada e para. */
  still?: boolean;
}

export interface FieldHandle {
  destroy: () => void;
  setStill: (still: boolean) => void;
}

interface Module {
  hx: number; hy: number;   // posição de origem
  x: number; y: number;     // posição atual
  vx: number; vy: number;
  rot: number; vrot: number;
  size: number;
  tone: number;             // 0 base, 1 médio, 2 destaque
}

const STEP = 13;            // distância entre módulos, em px de CSS
const RADIUS = 75.2;          // área de influência 20% menor que a primeira prévia
const MAX_DPR = 2;

const TONES = [
  'rgba(58,64,72,ALPHA)',
  'rgba(120,128,138,ALPHA)',
  'rgba(255,122,51,ALPHA)',
];

/** Desenha a placa na máscara. Cada tom de cinza vira um tom de módulo. */
function drawMask(ctx: CanvasRenderingContext2D, w: number, h: number) {
  ctx.clearRect(0,0,w,h);
  const x=w*.16,y=h*.23,bw=w*.69,bh=h*.44;
  ctx.fillStyle='#8a8a8a';ctx.fillRect(x,y,bw,bh);
  ctx.clearRect(x+15,y+13,bw-30,bh-28);
  ctx.fillStyle='#3a3a3a';ctx.fillRect(x+26,y+26,bw-52,bh-52);
  ctx.fillStyle='#ffffff';ctx.fillRect(x+bw*.17,y+bh*.3,bw*.22,10);ctx.fillRect(x+bw*.17,y+bh*.45,bw*.47,10);
  ctx.fillStyle='#8a8a8a';ctx.fillRect(x+bw*.17,y+bh*.6,bw*.33,10);
  ctx.beginPath();ctx.moveTo(x,y+bh+12);ctx.lineTo(x+bw,y+bh+12);ctx.lineTo(x+bw+30,y+bh+45);ctx.lineTo(x-30,y+bh+45);ctx.closePath();ctx.fill();
  ctx.fillStyle='#ffffff';ctx.fillRect(x+bw*.4,y+bh+16,bw*.2,8);
}

export function createModuleField(canvas: HTMLCanvasElement, options: FieldOptions = {}): FieldHandle {
  const context = canvas.getContext('2d');
  if (!context) return { destroy: () => {}, setStill: () => {} };

  const ctx: CanvasRenderingContext2D = context;
  let still = Boolean(options.still);
  let modules: Module[] = [];
  let width = 0;
  let height = 0;
  let dpr = 1;
  let frame = 0;
  let running = false;
  let visible = true;
  let entrance = 0;           // 0 a 1
  const pointer = { x: -9999, y: -9999, active: false };
  const target = { x: -9999, y: -9999 };

  function build() {
    const rect = canvas.getBoundingClientRect();
    width = Math.max(1, Math.round(rect.width));
    height = Math.max(1, Math.round(rect.height));
    dpr = Math.min(window.devicePixelRatio || 1, MAX_DPR);
    canvas.width = Math.round(width * dpr);
    canvas.height = Math.round(height * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    const mask = document.createElement('canvas');
    mask.width = width;
    mask.height = height;
    const mctx = mask.getContext('2d', { willReadFrequently: true });
    if (!mctx) return;
    drawMask(mctx, width, height);
    const data = mctx.getImageData(0, 0, width, height).data;

    modules = [];
    for (let y = STEP / 2; y < height; y += STEP) {
      for (let x = STEP / 2; x < width; x += STEP) {
        const index = ((y | 0) * width + (x | 0)) * 4;
        if (data[index + 3] < 120) continue;
        const level = data[index];
        const tone = level > 200 ? 2 : level > 100 ? 1 : 0;
        modules.push({
          hx: x, hy: y, x, y, vx: 0, vy: 0, rot: 0, vrot: 0,
          size: tone === 2 ? STEP - 3 : STEP - 4.5,
          tone,
        });
      }
    }

    if (still || entrance >= 1) {
      entrance = 1;
      for (const m of modules) { m.x = m.hx; m.y = m.hy; }
    } else {
      for (const m of modules) {
        const angle = Math.random() * Math.PI * 2;
        const distance = 60 + Math.random() * 220;
        m.x = m.hx + Math.cos(angle) * distance;
        m.y = m.hy + Math.sin(angle) * distance;
        m.rot = (Math.random() - 0.5) * 1.6;
      }
    }
  }

  function paint() {
    ctx.clearRect(0, 0, width, height);
    for (const m of modules) {
      const offset = Math.hypot(m.x - m.hx, m.y - m.hy);
      const alpha = Math.max(0.12, Math.min(1, entrance - offset / 320));
      if (alpha <= 0.05) continue;
      const shrink = Math.min(0.55, offset / 260);
      const size = m.size * (1 - shrink);
      ctx.save();
      ctx.translate(m.x, m.y);
      if (m.rot) ctx.rotate(m.rot);
      ctx.fillStyle = TONES[m.tone].replace('ALPHA', alpha.toFixed(3));
      ctx.fillRect(-size / 2, -size / 2, size, size);
      ctx.restore();
    }
  }

  function step() {
    frame = 0;
    if (!visible) { running = false; return; }

    if (entrance < 1) entrance = Math.min(1, entrance + 0.022);

    // O ponteiro persegue o alvo: a reação acompanha o movimento com atraso.
    if (pointer.active) {
      pointer.x += (target.x - pointer.x) * 0.22;
      pointer.y += (target.y - pointer.y) * 0.22;
    }

    let moving = entrance < 1;
    for (const m of modules) {
      if (pointer.active) {
        const dx = m.hx - pointer.x;
        const dy = m.hy - pointer.y;
        const distance = Math.hypot(dx, dy);
        if (distance < RADIUS) {
          const force = (1 - distance / RADIUS) ** 2 * 5.4;
          const nx = distance < 0.001 ? 0 : dx / distance;
          const ny = distance < 0.001 ? 0 : dy / distance;
          m.vx += nx * force;
          m.vy += ny * force;
          m.vrot += (nx - ny) * force * 0.02;
        }
      }
      // mola de volta ao lugar
      m.vx += (m.hx - m.x) * 0.055;
      m.vy += (m.hy - m.y) * 0.055;
      m.vrot += -m.rot * 0.08;
      m.vx *= 0.82;
      m.vy *= 0.82;
      m.vrot *= 0.8;
      m.x += m.vx;
      m.y += m.vy;
      m.rot += m.vrot;
      if (!moving && (Math.abs(m.vx) > 0.02 || Math.abs(m.vy) > 0.02 || Math.abs(m.x - m.hx) > 0.2 || Math.abs(m.y - m.hy) > 0.2)) {
        moving = true;
      }
    }

    paint();

    // Sem ponteiro e tudo no lugar: para de consumir quadros.
    if (moving || pointer.active) frame = requestAnimationFrame(step);
    else running = false;
  }

  function wake() {
    if (still || running || !visible) return;
    running = true;
    frame = requestAnimationFrame(step);
  }

  function onPointerMove(event: PointerEvent) {
    if (still || event.pointerType === 'touch') return;
    const rect = canvas.getBoundingClientRect();
    target.x = event.clientX - rect.left;
    target.y = event.clientY - rect.top;
    if (!pointer.active) { pointer.x = target.x; pointer.y = target.y; }
    pointer.active = true;
    wake();
  }

  function onPointerLeave() {
    pointer.active = false;
    wake();
  }

  const host = canvas.closest<HTMLElement>('.hx-stage') ?? canvas;
  host.addEventListener('pointermove', onPointerMove);
  host.addEventListener('pointerleave', onPointerLeave);
  host.addEventListener('pointercancel', onPointerLeave);

  const observer = typeof IntersectionObserver === 'function'
    ? new IntersectionObserver(entries => {
        visible = entries.some(entry => entry.isIntersecting);
        if (visible) wake();
      }, { threshold: 0 })
    : null;
  observer?.observe(canvas);

  function onVisibility() {
    visible = !document.hidden;
    if (visible) wake();
  }
  document.addEventListener('visibilitychange', onVisibility);

  const resize = typeof ResizeObserver === 'function'
    ? new ResizeObserver(() => { build(); if (still) paint(); else wake(); })
    : null;
  resize?.observe(canvas);

  build();
  if (still) paint();
  else wake();

  return {
    destroy() {
      cancelAnimationFrame(frame);
      running = false;
      host.removeEventListener('pointermove', onPointerMove);
      host.removeEventListener('pointerleave', onPointerLeave);
      host.removeEventListener('pointercancel', onPointerLeave);
      document.removeEventListener('visibilitychange', onVisibility);
      observer?.disconnect();
      resize?.disconnect();
      modules = [];
    },
    setStill(next: boolean) {
      still = next;
      if (still) {
        cancelAnimationFrame(frame);
        running = false;
        entrance = 1;
        for (const m of modules) { m.x = m.hx; m.y = m.hy; m.rot = 0; m.vx = 0; m.vy = 0; m.vrot = 0; }
        paint();
      } else {
        wake();
      }
    },
  };
}
