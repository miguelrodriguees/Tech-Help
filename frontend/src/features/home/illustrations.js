export function diagramKeyFor(nome        )             {
  const value = nome.toLowerCase();
  if (value.includes('hardware')) return 'hardware';
  if (value.includes('rede')) return 'redes';
  if (value.includes('software')) return 'software';
  if (value.includes('segur')) return 'seguranca';
  if (value.includes('servidor') || value.includes('cloud')) return 'servidores';
  return 'perifericos';
}

const open = '<svg viewBox="0 0 400 250" role="img" aria-label="ESCAPE">';

const bodies                                                   = {
  hardware: {
    alt: 'Computador de mesa aberto, com monitor, processador, memória e ventilação visíveis.',
    art: `
      <rect class="dg-plate" x="24" y="30" width="224" height="142" rx="12" stroke-width="2"/>
      <rect class="dg-part" x="40" y="46" width="192" height="106" rx="5" stroke-width="1.5"/>
      <path class="dg-line" d="M108 188h56m-28-16v16" stroke-width="5" stroke-linecap="round"/>
      <rect class="dg-plate" x="280" y="28" width="88" height="190" rx="12" stroke-width="2"/>
      <circle class="dg-part" cx="324" cy="79" r="25" stroke-width="2"/><circle class="dg-soft" cx="324" cy="79" r="8"/>
      <rect class="dg-part" x="298" y="120" width="52" height="34" rx="5" stroke-width="2"/>
      <rect class="dg-hot" x="304" y="128" width="20" height="18" rx="3"/>
      <rect class="dg-soft" x="331" y="128" width="12" height="4" rx="2"/><rect class="dg-soft" x="331" y="138" width="12" height="4" rx="2"/>
      <path class="dg-line-hot" d="M280 179h-24c-18 0-19-18-35-18h-30" stroke-width="3" stroke-linecap="round"/>
      <rect class="dg-hot" x="48" y="57" width="60" height="7" rx="3"/><rect class="dg-soft" x="48" y="75" width="116" height="7" rx="3"/>`,
  },
  software: {
    alt: 'Janela de software com código, menus, cursor e botão de execução.',
    art: `
      <rect class="dg-plate" x="28" y="24" width="344" height="204" rx="14" stroke-width="2"/>
      <path class="dg-line" d="M28 58h344M112 58v170" stroke-width="2"/>
      <circle class="dg-hot" cx="49" cy="41" r="5"/><circle class="dg-soft" cx="67" cy="41" r="5"/><circle class="dg-soft" cx="85" cy="41" r="5"/>
      <rect class="dg-soft" x="47" y="79" width="45" height="7" rx="3"/><rect class="dg-soft" x="47" y="101" width="54" height="7" rx="3"/>
      <rect class="dg-soft" x="47" y="123" width="38" height="7" rx="3"/><rect class="dg-hot" x="47" y="157" width="46" height="26" rx="6"/>
      <path class="dg-line-hot" d="M147 92l-20 20 20 20M221 92l20 20-20 20" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
      <path class="dg-line" d="M190 82l-18 62" stroke-width="3"/>
      <rect class="dg-soft" x="133" y="166" width="178" height="7" rx="3"/><rect class="dg-soft" x="133" y="187" width="122" height="7" rx="3"/>
      <path class="dg-hot" d="M326 160l20 12-20 12z"/>`,
  },
  redes: {
    alt: 'Roteador Wi-Fi conectando computador, celular e outros dispositivos.',
    art: `
      <path class="dg-line" d="M200 152 76 72M200 152 326 74M200 152 76 202M200 152 326 202" stroke-width="2" stroke-dasharray="6 7"/>
      <path class="dg-line-hot" d="M200 152 326 74" stroke-width="3"/>
      <rect class="dg-plate" x="136" y="148" width="128" height="52" rx="11" stroke-width="2"/>
      <path class="dg-line" d="M154 148v-38M246 148v-38" stroke-width="4" stroke-linecap="round"/>
      <circle class="dg-hot" cx="164" cy="175" r="5"/><circle class="dg-soft" cx="183" cy="175" r="5"/><circle class="dg-soft" cx="202" cy="175" r="5"/>
      <path class="dg-line-hot" d="M174 128c14-13 38-13 52 0M184 116c9-8 23-8 32 0" stroke-width="3" fill="none" stroke-linecap="round"/>
      <rect class="dg-part" x="33" y="36" width="88" height="58" rx="7" stroke-width="2"/><path class="dg-line" d="M65 102h25M77 94v8" stroke-width="3"/>
      <rect class="dg-part" x="304" y="37" width="42" height="75" rx="8" stroke-width="2"/><circle class="dg-hot" cx="325" cy="99" r="3"/>
      <rect class="dg-part" x="36" y="180" width="80" height="50" rx="8" stroke-width="2"/><rect class="dg-part" x="292" y="178" width="72" height="52" rx="8" stroke-width="2"/>`,
  },
  seguranca: {
    alt: 'Escudo digital com cadeado protegendo dispositivos conectados.',
    art: `
      <g class="dg-soft"><circle cx="60" cy="70" r="13"/><circle cx="340" cy="70" r="13"/><circle cx="61" cy="192" r="13"/><circle cx="339" cy="192" r="13"/></g>
      <path class="dg-line" d="M73 70h65M262 70h65M74 192h64M262 192h64" stroke-width="2" stroke-dasharray="5 6"/>
      <path class="dg-part" d="M200 20l76 28v62c0 54-36 91-76 112-40-21-76-58-76-112V48z" stroke-width="2"/>
      <rect class="dg-hot" x="163" y="104" width="74" height="61" rx="9"/>
      <path class="dg-line-hot" d="M177 104V88c0-31 46-31 46 0v16" stroke-width="8" fill="none" stroke-linecap="round"/>
      <circle class="dg-plate" cx="200" cy="132" r="9"/><path class="dg-plate" d="M196 138h8v14h-8z"/>
      <path class="dg-line-hot" d="M278 54l29-19M92 211l29-19" stroke-width="3"/>`,
  },
  servidores: {
    alt: 'Rack de servidores conectado a uma nuvem por uma linha de dados.',
    art: `
      <rect class="dg-plate" x="32" y="24" width="174" height="202" rx="12" stroke-width="2"/>
      <rect class="dg-part" x="48" y="43" width="142" height="38" rx="6" stroke-width="2"/><rect class="dg-part" x="48" y="91" width="142" height="38" rx="6" stroke-width="2"/>
      <rect class="dg-part" x="48" y="139" width="142" height="38" rx="6" stroke-width="2"/><rect class="dg-part" x="48" y="187" width="142" height="24" rx="6" stroke-width="2"/>
      <g class="dg-hot"><circle cx="65" cy="62" r="5"/><circle cx="65" cy="110" r="5"/><circle cx="65" cy="158" r="5"/></g>
      <g class="dg-soft"><rect x="82" y="58" width="90" height="8" rx="4"/><rect x="82" y="106" width="72" height="8" rx="4"/><rect x="82" y="154" width="84" height="8" rx="4"/></g>
      <path class="dg-line-hot" d="M206 125h39" stroke-width="3"/><circle class="dg-hot" cx="228" cy="125" r="5"/>
      <path class="dg-part" d="M269 177h67c26 0 42-15 42-35 0-18-13-33-32-36-5-29-29-48-58-48-30 0-54 20-59 49-18 4-30 18-30 35 0 20 17 35 42 35z" stroke-width="2"/>
      <path class="dg-line-hot" d="M256 130l20-19 18 19 25-25" stroke-width="4" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`,
  },
  perifericos: {
    alt: 'Mesa de trabalho com impressora, teclado, mouse e monitor.',
    art: `
      <rect class="dg-plate" x="24" y="26" width="196" height="126" rx="11" stroke-width="2"/><rect class="dg-part" x="39" y="41" width="166" height="94" rx="5" stroke-width="1.5"/>
      <path class="dg-line" d="M96 169h52m-26-17v17" stroke-width="5" stroke-linecap="round"/>
      <path class="dg-part" d="M48 191h151l-10 30H36z" stroke-width="2"/><path class="dg-line" d="M62 202h112" stroke-width="2" stroke-dasharray="7 5"/>
      <rect class="dg-part" x="257" y="87" width="112" height="93" rx="11" stroke-width="2"/><rect class="dg-plate" x="276" y="34" width="75" height="63" rx="5" stroke-width="2"/>
      <rect class="dg-plate" x="276" y="164" width="75" height="62" rx="5" stroke-width="2"/><rect class="dg-hot" x="338" y="108" width="17" height="9" rx="3"/>
      <path class="dg-soft" d="M223 186c17 0 27 14 27 31h-54c0-17 10-31 27-31z"/>
      <path class="dg-line-hot" d="M48 60h62M48 78h105" stroke-width="6" stroke-linecap="round"/>`,
  },
};

export function diagramFor(key            )         {
  const body = bodies[key];
  return open.replace('ESCAPE', body.alt) + body.art + '</svg>';
}

/** Conjunto de ferramentas do aluguel, visto de cima como um kit montado. */

export function rentalArt() { return `<svg viewBox="0 0 500 300" role="img" aria-label="Ilustração de um testador de cabos de rede e kit de chaves de precisão para manutenção de computadores">
 <ellipse cx="250" cy="268" rx="210" ry="12" fill="#000" opacity=".2"/>
 <path d="M98 77V43c0-31 117-31 117 0v35" fill="none" stroke="#ff8b4d" stroke-width="7"/>
 <rect x="47" y="74" width="109" height="164" rx="17" fill="#363c43" stroke="#656b72" stroke-width="2"/>
 <rect x="65" y="94" width="73" height="43" rx="7" fill="#14171b"/><text x="102" y="121" fill="#ffb488" text-anchor="middle" font-size="14" font-family="Arial">LAN TEST</text>
 ${Array.from({length:8},(_,i)=>`<circle cx="81" cy="${151+i*9}" r="2.4" fill="${i<4?'#ff8b4d':'#81878d'}"/><path d="M95 ${151+i*9}h25" stroke="#777d84" stroke-width="2"/>`).join('')}
 <rect x="185" y="77" width="62" height="159" rx="13" fill="#2c3138" stroke="#5e656d" stroke-width="2"/>
 <rect x="198" y="93" width="36" height="27" rx="4" fill="#17191d"/><path d="M199 145h34m-34 14h34m-34 14h34m-34 14h34" stroke="#858b93" stroke-width="3"/>
 <g transform="rotate(8 365 160)"><rect x="284" y="61" width="167" height="187" rx="19" fill="#252a30" stroke="#616870" stroke-width="2"/>
 <rect x="298" y="77" width="139" height="152" rx="11" fill="#15181c"/>
 ${Array.from({length:5},(_,i)=>`<rect x="${310+i*24}" y="98" width="8" height="91" rx="4" fill="#9da3aa"/><rect x="${307+i*24}" y="173" width="14" height="39" rx="6" fill="${i===2?'#ff8b4d':'#bf714c'}"/>`).join('')}
 </g></svg>`; }
