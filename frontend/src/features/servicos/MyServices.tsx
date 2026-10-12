import { useEffect, useRef, useState } from 'react';
import { api, errorMessage, type Session } from '../../services/api';
import { money } from '../propostas/proposals';
import ServiceReview from './ServiceReview';
import { useTechnicianNames } from '../propostas/useTechnicianNames';
import { technicianLabel } from '../propostas/technicians';
import { canCancel, availableAction, executeService, serviceDate, serviceStatus, serviceViews, type Service, type ServiceAction, type ServiceView } from './services';

export default function MyServices({ session, onBack }: { session: Session; onBack: () => void }) {
  const views = serviceViews(session);
  const [view, setView] = useState<ServiceView>(views[0] ?? 'cliente');
  return <section className="th-wizard">
    <h1 className="th-form-title">Meus serviços</h1>
    <button className="th-link" onClick={onBack}>← Voltar à Home</button>
    {views.length > 1 && <div className="th-actions" aria-label="Perfil de acompanhamento">{views.map(option => <button className="th-button secondary" key={option} aria-pressed={option === view} onClick={() => setView(option)}>{option === 'cliente' ? 'Como cliente' : 'Como técnico'}</button>)}</div>}
    {views.length ? <ServiceList key={`${session.idUsuario}-${view}`} session={session} view={view} /> : <p>Entre com uma conta de cliente ou técnico para acompanhar seus serviços.</p>}
  </section>;
}

function ServiceList({ session, view }: { session: Session; view: ServiceView }) {
  const [services, setServices] = useState<Service[]>([]);
  const [state, setState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [reload, setReload] = useState(0);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [confirm, setConfirm] = useState<{ id: number; action: ServiceAction } | null>(null);
  const [schedule, setSchedule] = useState('');
  const [busy, setBusy] = useState(false);
  const [mustRefresh, setMustRefresh] = useState(false);
  const lock = useRef(false);
  const names = useTechnicianNames(view === 'cliente' ? services.map(s => s.idTecnico) : [], reload);
  const id = view === 'cliente' ? session.idCliente : session.idTecnico;

  useEffect(() => {
    const controller = new AbortController();
    api.get<Service[]>(`/servicos/${view}/${id}`, { signal: controller.signal })
      .then(({ data }) => { if (!controller.signal.aborted) { setServices(data); setState('ready'); setMustRefresh(false); } })
      .catch(failure => { if (!controller.signal.aborted) { setError(errorMessage(failure)); setState('error'); } });
    return () => controller.abort();
  }, [view, id, reload]);

  function refresh() { setConfirm(null); setError(''); setNotice(''); setState('loading'); setReload(value => value + 1); }

  async function execute(service: Service, action: ServiceAction) {
    if (lock.current || mustRefresh || confirm?.id !== service.idServico || confirm.action !== action) return;
    lock.current = true; setBusy(true); setError(''); setNotice('');
    try {
      const updated = await executeService(service, action, session, schedule);
      setServices(previous => previous.map(item => item.idServico === updated.idServico ? updated : item));
      setNotice(`Serviço #${updated.idServico} ${({ iniciar: 'iniciado', concluir: 'concluído', agendar: 'agendado', cancelar: 'cancelado' })[action]} com sucesso.`);
    } catch (failure) {
      const message = failure instanceof Error && !('isAxiosError' in failure) ? failure.message : errorMessage(failure);
      setError(`${message} Atualize a lista para conferir a situação antes de tentar novamente.`);
      setMustRefresh(true);
    } finally { setConfirm(null); lock.current = false; setBusy(false); }
  }

  return <>
    <p className="th-sub">{view === 'cliente' ? 'Acompanhe aqui os atendimentos das propostas que você aceitou.' : 'Inicie o atendimento quando começar o trabalho e conclua somente depois de finalizá-lo.'}</p>
    <button className="th-button secondary" onClick={refresh} disabled={busy || state === 'loading'}>Atualizar serviços</button>
    {state === 'loading' && <p role="status">Carregando serviços…</p>}
    {error && <p role="alert" className="th-note th-error">{error}</p>}
    {notice && <p role="status" className="th-note">{notice}</p>}
    {state === 'ready' && (services.length ? <ul className="th-request-list">{services.map(service => {
      const action = view === 'tecnico' ? availableAction(service, session) : null;
      return <li className="th-flow-content" key={service.idServico}>
        <h2>Serviço #{service.idServico}</h2>
        <p><strong>{serviceStatus(service.status)}</strong> · {money(service.valorAcordado)}</p>
        <p>Proposta #{service.idProposta} · {view === 'cliente' ? technicianLabel(service.idTecnico, names) : `Cliente #${service.idCliente}`}</p>
        <dl className="th-review">
          <div><dt>Data combinada (horário de Brasília)</dt><dd>{serviceDate(service.dataAgendada, 'Ainda não informada')}</dd></div>
          <div><dt>Início do atendimento</dt><dd>{serviceDate(service.dataInicio, 'Ainda não iniciado')}</dd></div>
          <div><dt>Conclusão</dt><dd>{serviceDate(service.dataConclusao, 'Ainda não concluído')}</dd></div>
        </dl>
        {service.status === 'AGENDADO' && <p className="th-note">A proposta já foi aceita. Combine dia e horário com a outra pessoa antes do atendimento.</p>}
        {confirm?.id === service.idServico ? <div className="th-note">
          <p>{({ iniciar: 'Confirmar que você está iniciando este atendimento?', concluir: 'O trabalho foi finalizado? Confirmar marcará o serviço e o pedido como concluídos.', cancelar: 'Cancelar este atendimento e encerrar o pedido? Não será possível iniciar este serviço depois. Para contratar novamente, será necessário criar outro pedido.', agendar: 'Registre o dia e horário que você combinou com o cliente. Informe uma data futura, no horário de Brasília.' })[confirm.action]}</p>
          {confirm.action === 'agendar' && <label className="th-label">Dia e horário combinados<input className="th-field" type="datetime-local" value={schedule} disabled={busy} onChange={e => setSchedule(e.target.value)} /></label>}
          <div className="th-actions"><button className="th-button" disabled={busy || mustRefresh || (confirm.action === 'agendar' && !schedule)} onClick={() => execute(service, confirm.action)}>{busy ? 'Salvando…' : ({ iniciar: 'Confirmar início', concluir: 'Confirmar conclusão', cancelar: 'Confirmar cancelamento', agendar: 'Salvar agendamento' })[confirm.action]}</button><button className="th-link" disabled={busy} onClick={() => setConfirm(null)}>Voltar sem alterar</button></div>
        </div> : <div className="th-actions">
          {action && <button className="th-button" disabled={busy || mustRefresh} onClick={() => setConfirm({ id: service.idServico, action })}>{action === 'iniciar' ? 'Iniciar atendimento' : 'Concluir atendimento'}</button>}
          {view === 'tecnico' && action === 'iniciar' && <button className="th-button secondary" disabled={busy || mustRefresh} onClick={() => { setSchedule(service.dataAgendada?.slice(0, 16) ?? ''); setConfirm({ id: service.idServico, action: 'agendar' }); }}>{service.dataAgendada ? 'Alterar agendamento' : 'Agendar atendimento'}</button>}
          {canCancel(service, session) && <button className="th-link" disabled={busy || mustRefresh} onClick={() => setConfirm({ id: service.idServico, action: 'cancelar' })}>Cancelar atendimento</button>}
        </div>}
        {service.status === 'CANCELADO' && <p className="th-note">Atendimento e pedido cancelados. Para uma nova contratação, crie outro pedido.</p>}
        {service.status === 'CONCLUIDO' && <ServiceReview service={service} session={session} view={view} />}
      </li>;
    })}</ul> : <p>{view === 'cliente' ? 'Nenhum serviço contratado ainda. Eles aparecerão aqui quando você aceitar uma proposta em Meus pedidos.' : 'Nenhum serviço contratado ainda. Eles aparecerão aqui quando um cliente aceitar sua proposta.'}</p>)}
  </>;
}
