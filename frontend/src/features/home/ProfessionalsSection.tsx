import { ArrowRight } from 'lucide-react';
export default function ProfessionalsSection({ onAction }: { onAction: () => void }) {
 return (
<section className="pro-v2" id="profissionais" aria-labelledby="pro-title"><div className="th-inner pro-layout">
 <div className="pro-invite"><span className="section-label">PARA PROFISSIONAIS DE TI</span><h2 id="pro-title">Você sabe resolver.<br />Mostre seu trabalho.</h2>
 <p>Faça parte do TechHelp para apresentar suas especialidades e encontrar clientes que precisam do que você faz.</p>
 <button type="button" className="th-button" onClick={onAction}><span>Quero ser profissional</span> <ArrowRight size={20} aria-hidden="true" /></button>
 <small>Cadastro em preparação.</small></div>
 <div className="pro-benefits"><div><span>01</span><h3>Apresente seu perfil</h3><p>Suas especialidades, experiências e portfólio em um só lugar.</p></div><div><span>02</span><h3>Encontre solicitações</h3><p>Conheça o problema do cliente antes de enviar sua proposta.</p></div><div><span>03</span><h3>Combine o atendimento</h3><p>Acerte as condições com o cliente e acompanhe o serviço.</p></div></div>
 </div></section>
 );
}
