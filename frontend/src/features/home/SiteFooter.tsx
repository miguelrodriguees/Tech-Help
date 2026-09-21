import { Pause, Play } from 'lucide-react';

interface Props {
  paused: boolean;
  onTogglePaused: () => void;
  onSection: (id: string) => void;
}

export default function SiteFooter({ paused, onTogglePaused, onSection }: Props) {
  return (
    <footer className="th-footer">
      <div className="th-inner th-footer-inner">
        <div className="th-footer-brand">
          <strong>TechHelp</strong>
          <span>Serviços de TI e aluguel de ferramentas técnicas.</span>
          <span>Projeto Integrador de Software, SENAC Taboão da Serra.</span>
        </div>
        <div className="th-footer-links">
          <button type="button" onClick={() => onSection('como-funciona')}>Como funciona</button>
          <button type="button" onClick={() => onSection('ferramentas')}>Aluguel de ferramentas</button>
          <button type="button" onClick={() => onSection('profissionais')}>Sou profissional</button>
          <button type="button" aria-pressed={paused} onClick={onTogglePaused}>
            {paused ? <Play aria-hidden="true" /> : <Pause aria-hidden="true" />}
            {paused ? 'Retomar movimento' : 'Pausar movimento'}
          </button>
        </div>
      </div>
    </footer>
  );
}
