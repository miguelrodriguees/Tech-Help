import { useState } from 'react';
import type { Categoria } from '../../types/Categoria.ts';
import { areaFor } from '../solicitacao/flow.ts';
import type { CatalogState } from './catalog.ts';
import { iconFor } from './categoryIcon.ts';
import { diagramFor, diagramKeyFor } from './art/diagrams.ts';

interface Props {
  catalog: CatalogState;
  onChoose: (category: Categoria) => void;
  onRetry: () => void;
}

/**
 * Exploração das áreas: percorrer a lista troca o objeto desenhado ao lado.
 * O desenho é ilustrativo e não representa resultado, preço nem estoque.
 */
export default function AreaExplorer({ catalog, onChoose, onRetry }: Props) {
  const [active, setActive] = useState<number | null>(null);
  const list = catalog.status === 'ready' ? catalog.categories : [];
  const current = list.find(item => item.idCategoria === active) ?? list[0];

  return (
    <section className="hx-explorer" id="servicos" aria-labelledby="servicos-title">
      <div className="th-inner">
        <div className="hx-explorer-head">
          <h2 className="th-h2" id="servicos-title">Qual parte precisa de ajuda?</h2>
          <p className="th-body">
            Estas são as áreas cadastradas no TechHelp. Percorra para ver o que cada uma cobre.
            Três já têm o assistente de perguntas pronto.
          </p>
        </div>

        {catalog.status === 'loading' && <p className="th-body" role="status">Carregando as áreas…</p>}

        {catalog.status === 'error' && (
          <div className="hx-state" role="alert">
            <p><strong>As áreas não carregaram.</strong> Sem resposta do servidor.</p>
            <button type="button" className="th-button secondary" onClick={onRetry}>Tentar novamente</button>
          </div>
        )}

        {catalog.status === 'ready' && !list.length && (
          <div className="hx-state" role="status">
            <p><strong>Nenhuma área ativa no cadastro.</strong></p>
            <button type="button" className="th-button secondary" onClick={onRetry}>Atualizar</button>
          </div>
        )}

        {Boolean(list.length) && (
          <div className="hx-explorer-grid">
            <div className="hx-list">
              {list.map(category => {
                const Icon = iconFor(category.nome);
                const ready = Boolean(areaFor(category.nome));
                const isActive = current?.idCategoria === category.idCategoria;
                return (
                  <button
                    type="button"
                    key={category.idCategoria}
                    className={isActive ? 'hx-row is-active' : 'hx-row'}
                    onMouseEnter={() => setActive(category.idCategoria)}
                    onFocus={() => setActive(category.idCategoria)}
                    onClick={() => onChoose(category)}
                  >
                    <Icon aria-hidden="true" />
                    <span className="hx-row-name">{category.nome}</span>
                    <span className={ready ? 'th-chip live' : 'th-chip'}>{ready ? 'Perguntas prontas' : 'Em preparação'}</span>
                    <span className="hx-row-desc">{category.descricao ?? 'Sem descrição cadastrada.'}</span>
                  </button>
                );
              })}
            </div>

            {current && (
              <figure className="hx-figure">
                <div dangerouslySetInnerHTML={{ __html: diagramFor(diagramKeyFor(current.nome)) }} />
                <figcaption>
                  <b>{current.nome}</b>
                  {current.descricao ?? 'Sem descrição cadastrada.'}
                </figcaption>
              </figure>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
