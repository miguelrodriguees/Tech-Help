import { useEffect, useRef, useState } from 'react';
import { ArrowLeft, ArrowRight, BookmarkCheck, Check, Send } from 'lucide-react';
import { answersFor, questionsFor, remoteAllowed, selectAnswer, stages, urgencies, validateStage, type Draft } from './flow';

interface Props {
  draft: Draft;
  onChange: (draft: Draft) => void;
  onHome: () => void;
  onCategory: () => void;
  onPublish: () => void;
  saved: boolean;
  publishing?: boolean;
  publishError?: string;
}

function Options({ name, title, options, value, onChange }: {
  name: string; title: string; options: string[]; value: string; onChange: (value: string) => void;
}) {
  return <fieldset className="th-question">
    <legend>{title}</legend>
    <div className="th-options">
      {options.map(option => <label className="th-option" key={option}>
        <input type="radio" name={name} checked={value === option} onChange={() => onChange(option)} />
        <span>{option}</span>
      </label>)}
    </div>
  </fieldset>;
}

export default function RequestAssistant({ draft, onChange, onHome, onCategory, onPublish, saved, publishing = false, publishError = '' }: Props) {
  const [stage, setStage] = useState(0);
  const [error, setError] = useState('');
  const heading = useRef<HTMLHeadingElement>(null);
  const answers = answersFor(draft);
  const questions = questionsFor(draft);

  useEffect(() => { heading.current?.focus({ preventScroll: true }); }, [stage]);

  function go(next: number) {
    setError('');
    setStage(next);
    document.getElementById('solicitacao')?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth', block: 'start' });
  }

  function updateExtra(key: string, value: string) {
    if (!draft.area) return;
    onChange({ ...draft, branches: { ...draft.branches, [draft.area]: { ...answers, [key + 'Text']: value } } });
  }

  function advance() {
    const invalid = validateStage(draft, stage);
    if (invalid) { setError(invalid); return; }
    go(stage + 1);
  }

  function publish() {
    if(publishing)return;
    for (const step of [0, 1]) {
      const invalid = validateStage(draft, step);
      if (invalid) { go(step); setError(invalid); return; }
    }
    onPublish();
  }

  return <section className="th-wizard" id="solicitacao" aria-label="Solicitação de serviço">
    <div className="th-wizard-links">
      <button className="th-link" onClick={onHome}><ArrowLeft size={16} /> Voltar à Home</button>
      <button className="th-link" onClick={onCategory}>Trocar categoria</button>
    </div>
    <ol className="th-progress" aria-label="Etapas da solicitação">
      {stages.map((label, index) => <li className={index <= stage ? 'active' : ''} aria-current={index === stage ? 'step' : undefined} key={label}>
        <span className="th-step-dot">{index < stage ? <Check size={14} /> : index + 1}</span><span>{label}</span>
      </li>)}
    </ol>
    <div className="th-progress-track" aria-hidden="true"><div style={{ width: `${(stage + 1) * 25}%` }} /></div>
    <p className="th-sr-only" role="status">Etapa {stage + 1} de 4: {stages[stage]}</p>
    <form className="th-flow-content" key={stage} onSubmit={event => { event.preventDefault(); if (stage < 3) advance(); else publish(); }}>
      <fieldset disabled={publishing} style={{border:0,padding:0,minWidth:0}}>
      {stage === 0 && <>
        <p className="th-eyebrow">{draft.area}</p>
        <h1 className="th-form-title" ref={heading} tabIndex={-1}>Vamos entender seu problema.</h1>
        <p className="th-sub">Você pode responder “não sei”. Não precisa fazer um diagnóstico.</p>
        {questions.map(question => <div className="th-question-enter" key={question.key}>
          <Options name={question.key} title={question.title} options={question.options} value={answers[question.key] ?? ''} onChange={value => { setError(''); onChange(selectAnswer(draft, question.key, value)); }} />
          {['Outro', 'Outra'].includes(answers[question.key]) && <label className="th-label">Qual? <span>(opcional)</span><input className="th-field" maxLength={160} value={answers[question.key + 'Text'] ?? ''} onChange={e => updateExtra(question.key, e.target.value)} /></label>}
        </div>)}
        {draft.area === 'Software' && answers.system && answers.problem?.includes('programa') && <label className="th-label">Nome do programa <span>(se souber)</span><input className="th-field" maxLength={160} value={answers.programText ?? ''} onChange={e => updateExtra('program', e.target.value)} /></label>}
        {draft.area === 'Software' && answers.problem === 'Recuperar arquivos' && <p className="th-note">Conte o que aconteceu. Não pediremos que você formate ou faça testes que possam alterar seus arquivos.</p>}
      </>}
      {stage === 1 && <>
        <h1 className="th-form-title" ref={heading} tabIndex={-1}>Como você prefere receber ajuda?</h1>
        <p className="th-sub">{remoteAllowed(draft) ? 'Uma primeira orientação remota pode ser considerada. O técnico confirmará se é adequada.' : 'Para esse relato, propomos avaliação presencial. Você não precisa testar ou abrir o equipamento.'}</p>
        <Options name="mode" title="Tipo de atendimento" options={remoteAllowed(draft) ? ['Presencial', 'Remoto'] : ['Presencial']} value={draft.mode} onChange={mode => onChange({ ...draft, mode })} />
        <Options name="urgency" title="Quando você precisa?" options={urgencies} value={draft.urgency} onChange={urgency => onChange({ ...draft, urgency })} />
        <p className="th-footnote">Isso informa sua preferência. Atendimento no mesmo dia depende de disponibilidade e acordo.</p>
        {draft.mode === 'Presencial' && <section className="th-question-enter">
          <h2>Em qual região seria o atendimento?</h2>
          <p className="th-sub">Informe cidade/UF e bairro. O endereço completo será combinado depois.</p>
          <label className="th-label">Cidade e estado<input className="th-field" maxLength={120} autoComplete="address-level2" placeholder="Ex.: Taboão da Serra, SP" value={draft.city} onChange={e => onChange({ ...draft, city: e.target.value })} /></label>
          <label className="th-label">Bairro<input className="th-field" maxLength={120} placeholder="Informe o bairro" value={draft.district} onChange={e => onChange({ ...draft, district: e.target.value })} /></label>
        </section>}
      </>}
      {stage === 2 && <>
        <h1 className="th-form-title" ref={heading} tabIndex={-1}>Quer acrescentar algum detalhe?</h1>
        <p className="th-sub">Quando começou? Aconteceu algo antes? Escreva só o que souber. Esta etapa é opcional.</p>
        <label className="th-label">Detalhes<textarea className="th-field" maxLength={3000} value={draft.details} placeholder="Ex.: começou ontem. A luz acende, mas a tela fica preta." onChange={e => onChange({ ...draft, details: e.target.value })} /></label>
        <p className="th-footnote">Não inclua senhas nem dados de acesso. {draft.details.length}/3000 caracteres.</p>
      </>}
      {stage === 3 && <>
        <h1 className="th-form-title" ref={heading} tabIndex={-1}>Está tudo certo com seu pedido?</h1>
        <p className="th-sub">Confira suas respostas. Ainda não publicamos nada.</p>
        <dl className="th-review">
          <div><dt>Seu problema <button type="button" className="th-link" aria-label="Editar seu problema" onClick={() => go(0)}>Editar</button></dt><dd><strong>{draft.area}</strong>{questions.map(q => <p key={q.key}>{q.title}<br /><span>{answers[q.key]}{answers[q.key + 'Text'] ? ` — ${answers[q.key + 'Text']}` : ''}</span></p>)}{answers.programText && <p>Programa: {answers.programText}</p>}</dd></div>
          <div><dt>Atendimento <button type="button" className="th-link" aria-label="Editar atendimento" onClick={() => go(1)}>Editar</button></dt><dd>{draft.mode} · {draft.urgency}<br />{draft.mode === 'Presencial' ? `${draft.city} · ${draft.district}` : 'Sem endereço para atendimento remoto'}</dd></div>
          <div><dt>Detalhes <button type="button" className="th-link" aria-label="Editar detalhes" onClick={() => go(2)}>Editar</button></dt><dd>{draft.details || 'Nenhum detalhe adicional.'}</dd></div>
        </dl>
        <p className="th-note">Ao publicar, seu pedido será salvo na sua conta de cliente. Se necessário, entre antes de confirmar.</p>
      </>}
      {publishError && <p role="alert" className="th-note th-error">{publishError}</p>}
      {error && <p role="alert" className="th-note th-error">{error}</p>}
      <div className="th-actions">
        <button type="button" className="th-button secondary" onClick={() => stage === 0 ? onCategory() : go(stage - 1)}><ArrowLeft size={16} />Voltar</button>
        <button className="th-button" type="submit" disabled={publishing}>{publishing ? 'Publicando…' : stage === 3 ? 'Publicar solicitação' : stage === 2 ? 'Revisar solicitação' : 'Continuar'}{stage === 3 ? <Send size={16} /> : <ArrowRight size={16} />}</button>
      </div>
      </fieldset>
    </form>
    <p className="th-wizard-meta"><BookmarkCheck size={16} />{saved ? 'Rascunho guardado nesta aba. Fechar a aba encerra este armazenamento.' : 'Respostas preservadas enquanto a página estiver aberta. O navegador não permitiu guardar o rascunho.'}</p>
  </section>;
}
