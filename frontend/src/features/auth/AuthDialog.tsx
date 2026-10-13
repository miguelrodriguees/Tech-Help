import { useEffect, useRef, useState, type FormEvent } from 'react';
import { errorMessage, getSession, postProtected, type Session } from '../../services/api';

interface Props {
  initialProfessional?: boolean;
  onClose: () => void;
  onAuthenticated: (session: Session) => void;
}

export default function AuthDialog({ onClose, onAuthenticated, initialProfessional = false }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);
  const lock = useRef(false);
  const [mode, setMode] = useState<'login' | 'cadastro'>(initialProfessional ? 'cadastro' : 'login');
  const [role, setRole] = useState(initialProfessional ? 'tecnico' : 'cliente');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [email, setEmail] = useState('');

  useEffect(() => {
    const element = dialog.current;
    element?.showModal();
    return () => element?.close();
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (lock.current) return;
    const form = event.currentTarget;
    const data = new FormData(form);
    const senha = String(data.get('senha'));
    if (mode === 'cadastro' && new TextEncoder().encode(senha).length > 72) {
      setError('A senha é muito longa. Use uma senha menor, especialmente se ela contém emojis ou acentos.'); return;
    }
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try {
      if (mode === 'cadastro') {
        await postProtected(`/cadastro/${role}`, {
          nome: String(data.get('nome')).trim(), email: email.trim(), senha,
          cpf: String(data.get('cpf')).replace(/\D/g, ''),
          telefone: String(data.get('telefone')).trim() || null,
          ...(role === 'tecnico' ? { descricao: String(data.get('descricao')).trim(), anosExperiencia: Number(data.get('anosExperiencia')) } : {}),
        });
        form.reset(); setMode('login');
        setNotice('Conta criada. Entre com seu e-mail e senha para continuar.');
      } else {
        await postProtected('/auth/login', new URLSearchParams({ email: email.trim(), senha }));
        const session = await getSession();
        if (!session) throw new Error('Sessão indisponível');
        onAuthenticated(session);
      }
    } catch (failure) {
      setError(errorMessage(failure) + (mode === 'cadastro' ? ' Se o envio foi interrompido, tente entrar antes de cadastrar novamente.' : ''));
    } finally { lock.current = false; setBusy(false); }
  }

  return <dialog ref={dialog} className="th-dialog th-auth-dialog" aria-labelledby="auth-title"
    onCancel={event => { event.preventDefault(); if (!busy) onClose(); }}>
    <h2 id="auth-title">{mode === 'login' ? 'Entre na sua conta' : 'Crie sua conta'}</h2>
    <p>Seu rascunho permanece nesta aba enquanto você acessa a conta.</p>
    <form onSubmit={submit}>
      <fieldset disabled={busy} className="th-auth-fields">
        {mode === 'cadastro' && <>
          <label className="th-label">Quero me cadastrar como<select className="th-field" value={role} onChange={e => setRole(e.target.value)}><option value="cliente">Cliente — solicitar serviços</option><option value="tecnico">Técnico — prestar serviços</option></select></label>
          <label className="th-label">Nome completo<input name="nome" className="th-field" required maxLength={120} autoComplete="name" /></label>
          <label className="th-label">CPF<input name="cpf" className="th-field" required inputMode="numeric" pattern="[0-9]{11}" maxLength={11} title="Informe os 11 números do CPF, sem pontos ou traço." autoComplete="off" /><span>11 números, sem pontuação.</span></label>
          <label className="th-label">Telefone <span>(opcional)</span><input name="telefone" type="tel" className="th-field" maxLength={20} autoComplete="tel" /></label>
          {role === 'tecnico' && <>
            <label className="th-label">Anos de experiência<input name="anosExperiencia" type="number" className="th-field" required min={0} max={100} defaultValue={0} /></label>
            <label className="th-label">Sobre seu trabalho <span>(opcional)</span><textarea name="descricao" className="th-field" maxLength={3000} /></label>
            <p>A conta de técnico permite consultar oportunidades e enviar propostas de atendimento.</p>
          </>}
        </>}
        <label className="th-label">E-mail<input name="email" type="email" className="th-field" required maxLength={254} autoComplete="username" value={email} onChange={e => setEmail(e.target.value)} /></label>
        <label className="th-label">Senha<input name="senha" type="password" className="th-field" required minLength={mode === 'cadastro' ? 6 : undefined} maxLength={mode === 'cadastro' ? 72 : undefined} autoComplete={mode === 'cadastro' ? 'new-password' : 'current-password'} /></label>
        {mode === 'cadastro' && <p>Mínimo de 6 caracteres. Seus dados de acesso não são guardados no rascunho.</p>}
        {error && <p role="alert" className="th-note th-error">{error}</p>}
        {notice && <p role="status" className="th-note">{notice}</p>}
        <div className="th-actions"><button className="th-button" type="submit">{busy ? 'Aguarde…' : mode === 'login' ? 'Entrar' : 'Criar conta'}</button><button className="th-link" type="button" onClick={onClose}>Voltar</button></div>
        <button className="th-link th-auth-switch" type="button" onClick={() => { setMode(mode === 'login' ? 'cadastro' : 'login'); setError(''); setNotice(''); }}>{mode === 'login' ? 'Ainda não tenho conta' : 'Já tenho uma conta'}</button>
      </fieldset>
    </form>
  </dialog>;
}
