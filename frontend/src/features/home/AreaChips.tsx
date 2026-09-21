import { CircleHelp } from 'lucide-react';
import { iconFor } from './categoryIcon.ts';
import type { Categoria } from '../../types/Categoria.ts';
import { areaFor } from '../solicitacao/flow.ts';
import type { CatalogState } from './catalog.ts';


interface Props {
  catalog: CatalogState;
  onChoose: (category: Categoria) => void;
  onRetry: () => void;
  onUnsure?: () => void;
}

/** Entrada do assistente: as áreas prontas viram botões diretos. */
export default function AreaChips({ catalog, onChoose, onRetry, onUnsure }: Props) {
  if (catalog.status === 'loading') {
    return (
      <div className="hx-state" role="status" aria-busy="true">
        <div className="hx-skeleton" /><div className="hx-skeleton" /><div className="hx-skeleton" />
      </div>
    );
  }

  if (catalog.status === 'error') {
    return (
      <div className="hx-state" role="alert">
        <p><strong>As áreas não carregaram.</strong> O servidor do TechHelp não respondeu.</p>
        <button type="button" className="th-button secondary" onClick={onRetry}>Tentar novamente</button>
      </div>
    );
  }

  const ready = catalog.categories.filter(category => areaFor(category.nome));

  if (!ready.length) {
    return (
      <div className="hx-state" role="status">
        <p><strong>Nenhuma área está ativa agora.</strong> O cadastro vem do servidor e aparece aqui assim que existir.</p>
        <button type="button" className="th-button secondary" onClick={onRetry}>Atualizar</button>
      </div>
    );
  }

  return (
    <div className="hx-chips">
      {ready.map(category => {
        const Icon = iconFor(category.nome);
        return (
          <button type="button" className="hx-chip-btn" key={category.idCategoria} onClick={() => onChoose(category)}>
            <Icon aria-hidden="true" />
            {category.nome}
          </button>
        );
      })}
      {onUnsure && (
        <button type="button" className="hx-chip-btn ghost" onClick={onUnsure}>
          <CircleHelp aria-hidden="true" />
          Não sei dizer
        </button>
      )}
    </div>
  );
}


