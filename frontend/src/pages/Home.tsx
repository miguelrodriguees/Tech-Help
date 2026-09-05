import {
  ArrowRight,
  BadgeCheck,
  Cable,
  CheckCircle2,
  ChevronRight,
  Clock3,
  HardDrive,
  Laptop,
  MapPin,
  Network,
  PackageCheck,
  Printer,
  Search,
  Server,
  ShieldCheck,
  Star,
  Wrench,
} from "lucide-react";

const especialidades = [
  {
    icon: HardDrive,
    nome: "Hardware",
    descricao: "Montagem, upgrades, diagnóstico e manutenção.",
  },
  {
    icon: Laptop,
    nome: "Software",
    descricao: "Instalação, configuração e suporte de sistemas.",
  },
  {
    icon: Network,
    nome: "Redes",
    descricao: "Wi-Fi, roteadores, switches e infraestrutura.",
  },
  {
    icon: ShieldCheck,
    nome: "Segurança",
    descricao: "Proteção, análise e configuração de ambientes.",
  },
  {
    icon: Server,
    nome: "Servidores",
    descricao: "Servidores, serviços locais e ambientes em nuvem.",
  },
  {
    icon: Printer,
    nome: "Periféricos",
    descricao: "Impressoras, dispositivos e equipamentos.",
  },
];

function Home() {
  return (
    <div className="site">
      <header className="topbar">
        <div className="container topbar-content">
          <a href="/" className="brand">
            <div className="brand-mark">TH</div>

            <div className="brand-name">
              Tech<span>Help</span>
            </div>
          </a>

          <nav className="main-nav">
            <a href="#especialidades">Especialidades</a>
            <a href="#como-funciona">Como funciona</a>
            <a href="#ferramentas">Ferramentas</a>
            <a href="#tecnicos">Para técnicos</a>
          </nav>

          <div className="nav-actions">
            <button className="button-ghost">Entrar</button>

            <button className="button-dark">
              Publicar serviço
              <ArrowRight size={16} />
            </button>
          </div>
        </div>
      </header>

      <main>
        <section className="hero">
          <div className="hero-grid" />

          <div className="container hero-layout">
            <div className="hero-copy">
              <div className="eyebrow">
                <span className="eyebrow-dot" />
                Marketplace especializado em tecnologia
              </div>

              <h1>
                Suporte de TI,
                <br />
                <span>sem improviso.</span>
              </h1>

              <p className="hero-description">
                Encontre profissionais de tecnologia para resolver problemas,
                implementar projetos e cuidar da sua infraestrutura.
              </p>

              <div className="service-search">
                <div className="search-input">
                  <Search size={21} />

                  <div>
                    <small>O que você precisa resolver?</small>
                    <span>Ex.: meu notebook não está ligando</span>
                  </div>
                </div>

                <button>
                  Encontrar especialista
                  <ArrowRight size={18} />
                </button>
              </div>

              <div className="quick-services">
                <span>Mais procurados:</span>
                <button>Notebook</button>
                <button>Wi-Fi</button>
                <button>Formatação</button>
                <button>Rede empresarial</button>
              </div>

              <div className="hero-trust">
                <div>
                  <BadgeCheck size={18} />
                  Profissionais especializados
                </div>

                <div>
                  <ShieldCheck size={18} />
                  Processo transparente
                </div>
              </div>
            </div>

            <div className="product-preview">
              <div className="preview-bar">
                <div className="window-dots">
                  <span />
                  <span />
                  <span />
                </div>

                <div className="preview-label">
                  <span className="status-dot" />
                  Exemplo de solicitação
                </div>
              </div>

              <div className="preview-body">
                <div className="request-header">
                  <div className="request-icon">
                    <Laptop size={23} />
                  </div>

                  <div>
                    <span className="request-category">HARDWARE</span>
                    <h3>Notebook não inicia</h3>
                  </div>

                  <span className="request-status">Aberta</span>
                </div>

                <p className="request-description">
                  O equipamento liga, mas não apresenta imagem. Preciso de
                  diagnóstico e possível manutenção.
                </p>

                <div className="request-meta">
                  <div>
                    <MapPin size={15} />
                    Atendimento presencial
                  </div>

                  <div>
                    <Clock3 size={15} />
                    Urgência normal
                  </div>
                </div>

                <div className="proposal-title">
                  <div>
                    <strong>Propostas recebidas</strong>
                    <span>Compare profissionais antes de contratar</span>
                  </div>

                  <span className="proposal-count">4</span>
                </div>

                <div className="professional-card">
                  <div className="avatar avatar-one">RC</div>

                  <div className="professional-info">
                    <div>
                      <strong>Rafael Costa</strong>
                      <BadgeCheck size={15} />
                    </div>

                    <span>Suporte e manutenção</span>
                  </div>

                  <div className="professional-rating">
                    <Star size={14} fill="currentColor" />
                    4,9
                  </div>
                </div>

                <div className="professional-card">
                  <div className="avatar avatar-two">LM</div>

                  <div className="professional-info">
                    <div>
                      <strong>Lucas Martins</strong>
                      <BadgeCheck size={15} />
                    </div>

                    <span>Hardware e infraestrutura</span>
                  </div>

                  <div className="professional-rating">
                    <Star size={14} fill="currentColor" />
                    4,8
                  </div>
                </div>

                <button className="preview-action">
                  Ver todas as propostas
                  <ChevronRight size={17} />
                </button>
              </div>
            </div>
          </div>
        </section>

        <section className="expertise-strip">
          <div className="container expertise-strip-content">
            <span>Especialistas em</span>

            <div>
              <strong>Hardware</strong>
              <span />
              <strong>Software</strong>
              <span />
              <strong>Redes</strong>
              <span />
              <strong>Segurança</strong>
              <span />
              <strong>Servidores</strong>
              <span />
              <strong>Periféricos</strong>
            </div>
          </div>
        </section>

        <section className="specialties section" id="especialidades">
          <div className="container">
            <div className="section-top">
              <div>
                <span className="section-kicker">ESPECIALIDADES</span>

                <h2>
                  Tecnologia tem muitas áreas.
                  <br />
                  Aqui você encontra a pessoa certa.
                </h2>
              </div>

              <p>
                O TechHelp foi pensado exclusivamente para serviços de
                tecnologia. Cada solicitação pode chegar aos profissionais com
                a especialidade adequada.
              </p>
            </div>

            <div className="specialty-grid">
              {especialidades.map((item, index) => {
                const Icon = item.icon;

                return (
                  <article className="specialty-card" key={item.nome}>
                    <div className="specialty-card-top">
                      <span className="specialty-number">
                        0{index + 1}
                      </span>

                      <Icon size={24} />
                    </div>

                    <h3>{item.nome}</h3>
                    <p>{item.descricao}</p>

                    <button>
                      Explorar serviços
                      <ArrowRight size={16} />
                    </button>
                  </article>
                );
              })}
            </div>
          </div>
        </section>

        <section className="process-section" id="como-funciona">
          <div className="container process-layout">
            <div className="process-intro">
              <span className="section-kicker section-kicker-light">
                COMO FUNCIONA
              </span>

              <h2>
                Do problema à solução
                <br />
                em um fluxo simples.
              </h2>

              <p>
                Você mantém o controle da contratação do começo ao fim.
              </p>

              <button className="button-light">
                Criar solicitação
                <ArrowRight size={17} />
              </button>
            </div>

            <div className="process-steps">
              <article>
                <span className="step-number">01</span>

                <div>
                  <h3>Publique o que precisa</h3>
                  <p>
                    Informe o problema, tipo de atendimento, urgência e detalhes
                    necessários.
                  </p>
                </div>
              </article>

              <article>
                <span className="step-number">02</span>

                <div>
                  <h3>Compare propostas</h3>
                  <p>
                    Analise profissionais, valores e informações antes de
                    escolher quem irá realizar o serviço.
                  </p>
                </div>
              </article>

              <article>
                <span className="step-number">03</span>

                <div>
                  <h3>Acompanhe o serviço</h3>
                  <p>
                    O atendimento passa por etapas claras até sua conclusão e
                    avaliação.
                  </p>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section className="rental-section section" id="ferramentas">
          <div className="container rental-layout">
            <div className="rental-copy">
              <div className="feature-icon">
                <Wrench size={23} />
              </div>

              <span className="section-kicker">DIFERENCIAL TECHHELP</span>

              <h2>
                Ferramentas certas,
                <br />
                sem comprar tudo.
              </h2>

              <p>
                Técnicos podem encontrar ferramentas e kits disponíveis para
                aluguel, reduzindo o custo de entrada para determinados tipos de
                atendimento.
              </p>

              <div className="feature-list">
                <div>
                  <CheckCircle2 size={18} />
                  Kits organizados por finalidade
                </div>

                <div>
                  <CheckCircle2 size={18} />
                  Controle de retirada e devolução
                </div>

                <div>
                  <CheckCircle2 size={18} />
                  Valores de aluguel por período
                </div>
              </div>

              <button className="button-outline">
                Conhecer ferramentas
                <ArrowRight size={17} />
              </button>
            </div>

            <div className="kit-showcase">
              <div className="kit-background-card" />

              <div className="kit-card">
                <div className="kit-card-header">
                  <div className="kit-icon">
                    <PackageCheck size={24} />
                  </div>

                  <span>Disponível</span>
                </div>

                <span className="kit-type">KIT TÉCNICO</span>

                <h3>Kit de manutenção</h3>

                <p>
                  Conjunto básico para diagnóstico e manutenção de computadores.
                </p>

                <div className="kit-items">
                  <div>
                    <Wrench size={17} />
                    Chaves de precisão
                  </div>

                  <div>
                    <Cable size={17} />
                    Cabos e adaptadores
                  </div>

                  <div>
                    <HardDrive size={17} />
                    Ferramentas de diagnóstico
                  </div>
                </div>

                <div className="kit-footer">
                  <div>
                    <small>Diária a partir de</small>
                    <strong>R$ 50,00</strong>
                  </div>

                  <button>
                    Ver kit
                    <ArrowRight size={16} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="final-cta" id="tecnicos">
          <div className="container">
            <div className="cta-panel">
              <div>
                <span className="section-kicker section-kicker-light">
                  TECHHELP
                </span>

                <h2>Tem um problema de TI?</h2>
                <p>
                  Publique sua necessidade e encontre profissionais preparados
                  para ajudar.
                </p>

                <button className="button-light">
                  Solicitar serviço
                  <ArrowRight size={17} />
                </button>
              </div>

              <div className="cta-divider" />

              <div>
                <span className="section-kicker section-kicker-light">
                  PARA PROFISSIONAIS
                </span>

                <h2>Trabalha com tecnologia?</h2>
                <p>
                  Crie seu perfil profissional e encontre novas oportunidades de
                  serviço.
                </p>

                <button className="button-transparent">
                  Quero ser técnico
                  <ArrowRight size={17} />
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer>
        <div className="container footer-content">
          <a href="/" className="brand brand-footer">
            <div className="brand-mark">TH</div>
            <div className="brand-name">
              Tech<span>Help</span>
            </div>
          </a>

          <p>Marketplace especializado em serviços de tecnologia.</p>

          <span>Projeto TechHelp</span>
        </div>
      </footer>
    </div>
  );
}

export default Home;