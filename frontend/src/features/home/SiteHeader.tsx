import { useEffect, useRef } from 'react';
import { Menu, Wrench, X } from 'lucide-react';

interface Props {
  menuOpen: boolean;
  accountLabel?: string;
  onToggleMenu: (open: boolean) => void;
  onBrand: () => void;
  onSection: (id: string) => void;
  onSignIn: () => void;
}

const links = [
  { id: 'servicos', label: 'Serviços' },
  { id: 'como-funciona', label: 'Como funciona' },
  { id: 'ferramentas', label: 'Aluguel de ferramentas', rental: true },
  { id: 'profissionais', label: 'Sou profissional' },
];

export default function SiteHeader({ menuOpen, accountLabel, onToggleMenu, onBrand, onSection, onSignIn }: Props) {
  const toggle = useRef<HTMLButtonElement>(null);
  const panel = useRef<HTMLElement>(null);

  // Abrir o menu leva o foco para o primeiro item; Esc fecha e devolve o foco
  // ao botão, para quem navega por teclado não ficar preso.
  useEffect(() => {
    if (!menuOpen) return;
    panel.current?.querySelector<HTMLButtonElement>('button')?.focus();
    function onKey(event: KeyboardEvent) {
      if (event.key !== 'Escape') return;
      onToggleMenu(false);
      toggle.current?.focus();
    }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [menuOpen, onToggleMenu]);

  return (
    <>
      <header className="th-header">
        <div className="th-inner th-header-inner">
          <button type="button" className="th-brand" onClick={onBrand} aria-label="TechHelp, ir para o início">
            <svg className="th-mark" viewBox="0 0 26 26" aria-hidden="true">
              <rect className="th-mark-body" x="1" y="1" width="24" height="24" rx="7" strokeWidth="1.5" />
              <path className="th-mark-line" d="M6.5 18 12 12.5 15.5 16" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" />
              <circle className="th-mark-signal" cx="18.5" cy="8.5" r="3" />
            </svg>
            TechHelp
          </button>

          <nav className="th-nav" aria-label="Navegação principal">
            {links.map(link => (
              <button
                type="button"
                key={link.id}
                className={link.rental ? 'th-nav-link th-nav-rental' : 'th-nav-link'}
                onClick={() => onSection(link.id)}
              >
                {link.rental && <Wrench aria-hidden="true" />}
                {link.label}
              </button>
            ))}
            <button type="button" className="th-button secondary" onClick={onSignIn}>{accountLabel ?? 'Entrar'}</button>
          </nav>

          <button
            type="button"
            ref={toggle}
            className="th-button secondary th-menu-toggle"
            aria-expanded={menuOpen}
            aria-controls="menu-mobile"
            onClick={() => onToggleMenu(!menuOpen)}
          >
            {menuOpen ? <X aria-hidden="true" size={18} /> : <Menu aria-hidden="true" size={18} />}
            {menuOpen ? 'Fechar' : 'Menu'}
          </button>
        </div>
      </header>

      <nav
        id="menu-mobile"
        ref={panel}
        className="th-mobile-nav"
        aria-label="Menu"
        hidden={!menuOpen}
      >
        <div className="th-inner">
          {links.map(link => (
            <button
              type="button"
              key={link.id}
              className={link.rental ? 'th-nav-rental' : undefined}
              onClick={() => onSection(link.id)}
            >
              {link.rental && <Wrench aria-hidden="true" />}
              {link.label}
            </button>
          ))}
          <button type="button" onClick={onSignIn}>{accountLabel ?? 'Entrar'}</button>
        </div>
      </nav>
    </>
  );
}
