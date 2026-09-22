import { ArrowRight } from 'lucide-react';
import { rentalArt } from './art/rentalArt.ts';
export default function RentalSection({ onAction }: { onAction: () => void }) {
 return (
<section className="rent-v2" id="ferramentas" aria-labelledby="aluguel-title"><div className="th-inner rent-layout">
 <div><span className="section-label">FERRAMENTAS E KITS</span><h2 id="aluguel-title">Alugue a ferramenta.<br />Faça o trabalho.</h2>
 <p>Equipamentos para manutenção de computadores e instalação de redes, pelo período que você precisar.</p>
 <p className="rent-explanation">Retire e devolva no TechHelp ou solicite entrega e coleta no seu endereço. Você aprova as taxas antes de confirmar.</p>
 <button className="th-button secondary" type="button" onClick={onAction}><span>Conhecer o aluguel</span> <ArrowRight size={20} aria-hidden="true" /></button>
 <small>Equipamentos do TechHelp. Transporte mediante orçamento.</small></div>
 <figure className="rent-art"><div dangerouslySetInnerHTML={{ __html: rentalArt }} /><figcaption><span>Testador de cabos de rede</span><span>Kit de chaves de precisão</span></figcaption><small>Ilustrações dos tipos de equipamento previstos.</small></figure>
 </div></section>
 );
}
