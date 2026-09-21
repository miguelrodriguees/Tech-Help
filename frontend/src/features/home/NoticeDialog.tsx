import { useEffect, useRef } from 'react';

export interface Notice { title: string; body: string }

interface Props {
  notice: Notice | null;
  onClose: () => void;
}

/**
 * Diálogo usado quando um controle não tem destino real ainda. Ele explica a
 * situação em vez de simular sucesso.
 */
export default function NoticeDialog({ notice, onClose }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const element = dialog.current;
    if (!element) return;
    if (notice && !element.open) element.showModal();
    if (!notice && element.open) element.close();
  }, [notice]);

  return (
    <dialog
      className="th-dialog"
      ref={dialog}
      aria-labelledby="th-dialog-title"
      onClose={onClose}
      onClick={event => { if (event.target === event.currentTarget) dialog.current?.close(); }}
    >
      <h2 id="th-dialog-title">{notice?.title}</h2>
      <p>{notice?.body}</p>
      <button type="button" className="th-button" onClick={() => dialog.current?.close()}>Entendi</button>
    </dialog>
  );
}
