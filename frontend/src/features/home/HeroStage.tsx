import { useEffect, useRef } from 'react';
import { BookmarkCheck } from 'lucide-react';
import type { Categoria } from '../../types/Categoria.ts';
import type { CatalogState } from './catalog.ts';
import AreaChips from './AreaChips.tsx';
import { createModuleField, type FieldHandle } from './art/moduleField.ts';

interface Props {
  catalog: CatalogState;
  hasDraft: boolean;
  paused: boolean;
  onChoose: (category: Categoria) => void;
  onRetry: () => void;
  onUnsure: () => void;
  onResume: () => void;
}

export default function HeroStage({ catalog, hasDraft, paused, onChoose, onRetry, onUnsure, onResume }: Props) {
  const canvas = useRef<HTMLCanvasElement>(null);
  const field = useRef<FieldHandle | null>(null);

  useEffect(() => {
    if (!canvas.current) return;
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const coarse = window.matchMedia('(pointer: coarse)').matches;
    // A arte é melhoria opcional: se o canvas falhar, a página segue inteira.
    try {
      field.current = createModuleField(canvas.current, { still: reduce || coarse });
    } catch {
      field.current = null;
    }
    return () => { field.current?.destroy(); field.current = null; };
  }, []);

  useEffect(() => { field.current?.setStill(paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches || window.matchMedia('(pointer: coarse)').matches); }, [paused]);

  return (
    <section className="hx-stage" aria-labelledby="hero-title">
      <div className="hx-canvas-wrap" aria-hidden="true">
        <canvas ref={canvas} />
      </div>
      <div className="hx-canvas-hit" aria-hidden="true" />
      <div className="hx-scrim" aria-hidden="true" />

      <div className="th-inner hx-stage-inner">
        <h1 className="th-display hx-title th-enter th-enter-1" id="hero-title">
          Seu problema em TI.<br /><span className="hx-soft">Um começo simples.</span>
        </h1>
        <p className="th-lead th-enter th-enter-2">
          Notebook, programas ou conexão: conte o que aconteceu. O TechHelp ajuda você a organizar o pedido e encontrar atendimento.
        </p>
        {hasDraft && (
          <button type="button" className="th-link th-enter th-enter-2" onClick={onResume} style={{ justifySelf: 'start' }}>
            <BookmarkCheck aria-hidden="true" size={16} />
            Continuar de onde parei
          </button>
        )}

        <div className="hx-command th-enter th-enter-3">
          <div className="hx-command-label">
            <b>Descrever meu problema</b>
            <span>Comece sem criar uma conta.</span>
          </div>
          <AreaChips catalog={catalog} onChoose={onChoose} onRetry={onRetry} onUnsure={onUnsure} />
          <p className="hx-command-note">
            Publicar o pedido vai exigir conta, e essa parte ainda está em desenvolvimento.
            Até lá suas respostas ficam guardadas nesta aba.
          </p>
        </div>
      </div>
    </section>
  );
}
