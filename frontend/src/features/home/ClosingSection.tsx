interface Props {
  hasDraft: boolean;
  onStart: () => void;
}

export default function ClosingSection({ hasDraft, onStart }: Props) {
  return (
    <section className="hx-end" aria-labelledby="fechar-title">
      <div className="th-inner hx-end-inner">
        <div className="hx-end-strip" aria-hidden="true">
          {Array.from({ length: 11 }, (_, index) => <i key={index} />)}
        </div>
        <h2 className="th-h2" id="fechar-title">
          {hasDraft ? 'Seu pedido está esperando você' : 'Comece pelo que você já sabe'}
        </h2>
        <p>
          {hasDraft
            ? 'As respostas continuam guardadas nesta aba. Dá para revisar e mudar tudo antes de publicar.'
            : 'Uma frase comum basta. O resto das perguntas é com a gente.'}
        </p>
        <button type="button" className="th-button" onClick={onStart}>
          {hasDraft ? 'Retomar meu pedido' : 'Descrever meu problema'}
        </button>
      </div>
    </section>
  );
}
