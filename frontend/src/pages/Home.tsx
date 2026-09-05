import { useEffect, useMemo, useState, type ElementType } from "react";
import {
  Activity,
  ArrowRight,
  BadgeCheck,
  Boxes,
  Cable,
  Check,
  ChevronRight,
  CircleCheck,
  Clock3,
  Cpu,
  Database,
  HardDrive,
  Laptop,
  MapPin,
  MessageSquareText,
  Network,
  PackageOpen,
  Printer,
  Router,
  Search,
  Server,
  ShieldCheck,
  SlidersHorizontal,
  Star,
  Terminal,
  UserRoundCheck,
  Wrench,
} from "lucide-react";

import { api } from "../services/api";
import type { Categoria } from "../types/Categoria";

type ApiStatus = "checking" | "online" | "demo";

const categoriasFallback: Categoria[] = [
  {
    idCategoria: 1,
    nome: "Hardware",
    descricao: "Diagnóstico, manutenção, montagem e upgrades.",
    ativo: true,
    dataCadastro: null,
  },
  {
    idCategoria: 2,
    nome: "Software",
    descricao: "Instalação, configuração e suporte de sistemas.",
    ativo: true,
    dataCadastro: null,
  },
  {
    idCategoria: 3,
    nome: "Redes",
    descricao: "Wi-Fi, roteadores, switches e conectividade.",
    ativo: true,
    dataCadastro: null,
  },
  {
    idCategoria: 4,
    nome: "Segurança",
    descricao: "Proteção, configuração e análise de ambientes.",
    ativo: true,
    dataCadastro: null,
  },
  {
    idCategoria: 5,
    nome: "Servidores e Cloud",
    descricao: "Servidores locais, serviços e ambientes em nuvem.",
    ativo: true,
    dataCadastro: null,
  },
  {
    idCategoria: 6,
    nome: "Periféricos",
    descricao: "Impressoras, dispositivos e equipamentos de apoio.",
    ativo: true,
    dataCadastro: null,
  },
];

function getCategoriaIcon(nome: string): ElementType {
  const valor = nome.toUpperCase();

  if (valor.includes("HARDWARE")) return HardDrive;
  if (valor.includes("SOFTWARE")) return Cpu;
  if (valor.includes("REDE")) return Network;
  if (valor.includes("SEGUR")) return ShieldCheck;
  if (valor.includes("SERVID") || valor.includes("CLOUD")) return Server;
  if (valor.includes("IMPRESS") || valor.includes("PERIF")) return Printer;

  return Boxes;
}

function Home() {
  const [categorias, setCategorias] =
    useState<Categoria[]>(categoriasFallback);

  const [apiStatus, setApiStatus] =
    useState<ApiStatus>("checking");

  useEffect(() => {
    let ativo = true;

    async function carregarCategorias() {
      try {
        const resposta =
          await api.get<Categoria[]>("/categorias");

        if (!ativo) return;

        const categoriasAtivas =
          resposta.data.filter((categoria) => categoria.ativo);

        if (categoriasAtivas.length > 0) {
          setCategorias(categoriasAtivas);
        }

        setApiStatus("online");
      } catch {
        if (!ativo) return;

        setCategorias(categoriasFallback);
        setApiStatus("demo");
      }
    }

    carregarCategorias();

    return () => {
      ativo = false;
    };
  }, []);

  const statusInfo = useMemo(() => {
    if (apiStatus === "online") {
      return {
        titulo: "Sistema conectado",
        descricao: "Spring Boot · MariaDB",
        classe: "online",
      };
    }

    if (apiStatus === "demo") {
      return {
        titulo: "Modo demonstração",
        descricao: "API indisponível",
        classe: "demo",
      };
    }

    return {
      titulo: "Verificando sistema",
      descricao: "Conectando à API",
      classe: "checking",
    };
  }, [apiStatus]);

  return (
    <div className="techhelp-site">
      <header className="site-header">
        <div className="container header-inner">
          <a href="/" className="brand">
            <span className="brand-symbol">
              <Terminal size={17} strokeWidth={2.2} />
            </span>

            <span className="brand-word">
              Tech<span>Help</span>
            </span>
          </a>

          <nav className="desktop-nav">
            <a href="#servicos">Serviços</a>
            <a href="#fluxo">Como funciona</a>
            <a href="#profissionais">Profissionais</a>
            <a href="#ferramentas">Ferramentas</a>
          </nav>

          <div className="header-actions">
            <button className="text-button" type="button">
              Entrar
            </button>

            <button className="solid-button compact" type="button">
              Publicar solicitação
              <ArrowRight size={15} />
            </button>
          </div>
        </div>
      </header>

      <main>
        <section className="hero">
          <div className="hero-pattern" />

          <div className="container hero-layout">
            <div className="hero-main">
              <div className="hero-kicker">
                <span className="kicker-line" />
                Plataforma especializada em serviços de TI
              </div>

              <h1>
                Tecnologia resolvida
                <span> por quem entende.</span>
              </h1>

              <p className="hero-lead">
                Publique uma necessidade, compare profissionais
                especializados e acompanhe cada etapa do serviço
                em um único ambiente.
              </p>

              <div className="problem-search">
                <div className="problem-input">
                  <Search size={20} />

                  <div>
                    <small>O que precisa ser resolvido?</small>
                    <span>
                      Ex.: meu computador liga, mas não apresenta imagem
                    </span>
                  </div>
                </div>

                <button type="button">
                  Encontrar profissional
                  <ArrowRight size={17} />
                </button>
              </div>

              <div className="search-suggestions">
                <span>Buscas frequentes</span>
                <button type="button">Wi-Fi</button>
                <button type="button">Notebook</button>
                <button type="button">Formatação</button>
                <button type="button">Rede empresarial</button>
              </div>

              <div className="system-status-row">
                <div className={`system-indicator ${statusInfo.classe}`}>
                  <span className="indicator-dot" />

                  <div>
                    <strong>{statusInfo.titulo}</strong>
                    <span>{statusInfo.descricao}</span>
                  </div>
                </div>

                <div className="hero-proof">
                  <BadgeCheck size={16} />
                  Profissionais especializados
                </div>

                <div className="hero-proof">
                  <ShieldCheck size={16} />
                  Fluxo transparente
                </div>
              </div>
            </div>

            <div className="operations-console">
              <div className="console-topbar">
                <div className="console-title">
                  <Activity size={14} />
                  Central de atendimento
                </div>

                <span className="demo-label">
                  CENÁRIO DEMONSTRATIVO
                </span>
              </div>

              <div className="console-request">
                <div className="request-code-row">
                  <span>SOLICITAÇÃO</span>
                  <strong>#084</strong>
                </div>

                <div className="request-title-row">
                  <div className="request-main-icon">
                    <Laptop size={22} />
                  </div>

                  <div>
                    <span className="mono-label">HARDWARE</span>
                    <h2>Notebook não inicia</h2>
                  </div>

                  <span className="status-badge open">
                    ABERTA
                  </span>
                </div>

                <p>
                  O equipamento liga, mas não apresenta imagem.
                  Necessário diagnóstico e possível manutenção.
                </p>

                <div className="request-properties">
                  <div>
                    <MapPin size={14} />
                    <span>
                      <small>ATENDIMENTO</small>
                      Presencial
                    </span>
                  </div>

                  <div>
                    <Clock3 size={14} />
                    <span>
                      <small>URGÊNCIA</small>
                      Normal
                    </span>
                  </div>
                </div>
              </div>

              <div className="proposal-section">
                <div className="console-section-heading">
                  <div>
                    <span>PROPOSTAS</span>
                    <strong>04 recebidas</strong>
                  </div>

                  <button type="button">
                    Comparar
                    <SlidersHorizontal size={14} />
                  </button>
                </div>

                <div className="professional-row featured">
                  <div className="tech-avatar dark">
                    RC
                  </div>

                  <div className="tech-info">
                    <div>
                      <strong>Rafael Costa</strong>
                      <BadgeCheck size={14} />
                    </div>

                    <span>Suporte e manutenção</span>
                  </div>

                  <div className="tech-rating">
                    <Star size={13} fill="currentColor" />
                    4,9
                  </div>

                  <div className="proposal-value">
                    <small>PROPOSTA</small>
                    <strong>R$ 180</strong>
                  </div>
                </div>

                <div className="professional-row">
                  <div className="tech-avatar accent">
                    LM
                  </div>

                  <div className="tech-info">
                    <div>
                      <strong>Lucas Martins</strong>
                      <BadgeCheck size={14} />
                    </div>

                    <span>Hardware e infraestrutura</span>
                  </div>

                  <div className="tech-rating">
                    <Star size={13} fill="currentColor" />
                    4,8
                  </div>

                  <div className="proposal-value">
                    <small>PROPOSTA</small>
                    <strong>R$ 210</strong>
                  </div>
                </div>
              </div>

              <div className="console-footer">
                <div>
                  <MessageSquareText size={15} />
                  Conversa disponível após contato
                </div>

                <button type="button">
                  Ver solicitação
                  <ChevronRight size={15} />
                </button>
              </div>
            </div>
          </div>
        </section>

        <section className="system-band">
          <div className="container system-band-inner">
            <span>TECHHELP / SERVICE NETWORK</span>

            <div>
              <strong>Hardware</strong>
              <i />
              <strong>Software</strong>
              <i />
              <strong>Redes</strong>
              <i />
              <strong>Segurança</strong>
              <i />
              <strong>Infraestrutura</strong>
            </div>

            <span>BR / 2026</span>
          </div>
        </section>

        <section
          className="categories-section section"
          id="servicos"
        >
          <div className="container">
            <div className="section-heading split-heading">
              <div>
                <span className="eyebrow">
                  SERVIÇOS ESPECIALIZADOS
                </span>

                <h2>
                  Um ponto de acesso para
                  <br />
                  diferentes áreas de TI.
                </h2>
              </div>

              <div className="section-side-copy">
                <p>
                  As categorias abaixo são carregadas pela API do
                  TechHelp quando o backend está disponível.
                </p>

                <div className="database-reference">
                  <Database size={15} />
                  <span>
                    {String(categorias.length).padStart(2, "0")} categorias
                  </span>
                </div>
              </div>
            </div>

            <div className="category-table">
              {categorias.map((categoria, index) => {
                const Icon =
                  getCategoriaIcon(categoria.nome);

                return (
                  <article
                    className="category-row"
                    key={categoria.idCategoria}
                  >
                    <span className="row-number">
                      {String(index + 1).padStart(2, "0")}
                    </span>

                    <div className="category-icon">
                      <Icon size={21} />
                    </div>

                    <h3>{categoria.nome}</h3>

                    <p>
                      {categoria.descricao ||
                        "Serviços especializados disponíveis na plataforma."}
                    </p>

                    <button type="button">
                      Explorar
                      <ArrowRight size={15} />
                    </button>
                  </article>
                );
              })}
            </div>
          </div>
        </section>

        <section
          className="workflow-section"
          id="fluxo"
        >
          <div className="container">
            <div className="workflow-top">
              <div>
                <span className="eyebrow light">
                  FLUXO OPERACIONAL
                </span>

                <h2>
                  Do chamado à conclusão,
                  <br />
                  sem perder o contexto.
                </h2>
              </div>

              <p>
                O fluxo do TechHelp acompanha a mesma lógica
                implementada no backend: solicitação, proposta,
                contratação, execução e avaliação.
              </p>
            </div>

            <div className="workflow-grid">
              <article>
                <span className="workflow-index">01</span>
                <Search size={22} />

                <div>
                  <h3>Solicitação</h3>
                  <p>
                    O cliente descreve o problema e define os
                    detalhes do atendimento.
                  </p>
                </div>

                <span className="workflow-state">
                  ABERTA
                </span>
              </article>

              <article>
                <span className="workflow-index">02</span>
                <UserRoundCheck size={22} />

                <div>
                  <h3>Propostas</h3>
                  <p>
                    Técnicos especializados analisam a demanda
                    e enviam condições.
                  </p>
                </div>

                <span className="workflow-state">
                  EM NEGOCIAÇÃO
                </span>
              </article>

              <article>
                <span className="workflow-index">03</span>
                <CircleCheck size={22} />

                <div>
                  <h3>Contratação</h3>
                  <p>
                    Uma proposta é escolhida e passa a existir
                    um serviço vinculado.
                  </p>
                </div>

                <span className="workflow-state">
                  AGENDADO
                </span>
              </article>

              <article>
                <span className="workflow-index">04</span>
                <Activity size={22} />

                <div>
                  <h3>Atendimento</h3>
                  <p>
                    Cliente e técnico acompanham o andamento
                    até a conclusão.
                  </p>
                </div>

                <span className="workflow-state">
                  EM ANDAMENTO
                </span>
              </article>

              <article>
                <span className="workflow-index">05</span>
                <Star size={22} />

                <div>
                  <h3>Avaliação</h3>
                  <p>
                    O histórico termina com a avaliação do
                    serviço realizado.
                  </p>
                </div>

                <span className="workflow-state">
                  CONCLUÍDO
                </span>
              </article>
            </div>
          </div>
        </section>

        <section
          className="professionals-section section"
          id="profissionais"
        >
          <div className="container professional-layout">
            <div className="professional-copy">
              <span className="eyebrow">
                REDE DE PROFISSIONAIS
              </span>

              <h2>
                Mais contexto.
                <br />
                Menos tentativa e erro.
              </h2>

              <p>
                Perfis profissionais podem reunir especialidades,
                certificações, portfólio, avaliações e histórico
                dentro da própria plataforma.
              </p>

              <ul>
                <li>
                  <Check size={16} />
                  Especialidades técnicas
                </li>

                <li>
                  <Check size={16} />
                  Certificações profissionais
                </li>

                <li>
                  <Check size={16} />
                  Portfólio de trabalhos
                </li>

                <li>
                  <Check size={16} />
                  Avaliações de atendimentos
                </li>
              </ul>

              <button className="outline-button" type="button">
                Encontrar profissionais
                <ArrowRight size={16} />
              </button>
            </div>

            <div className="profile-interface">
              <div className="profile-interface-header">
                <span>PERFIL PROFISSIONAL</span>
                <span>#TEC-024</span>
              </div>

              <div className="profile-main">
                <div className="large-avatar">
                  GM
                </div>

                <div className="profile-heading">
                  <div>
                    <h3>Gabriel Mendes</h3>
                    <BadgeCheck size={17} />
                  </div>

                  <span>
                    Infraestrutura · Redes · Hardware
                  </span>
                </div>

                <div className="profile-score">
                  <Star size={15} fill="currentColor" />
                  <strong>4,9</strong>
                  <small>38 avaliações</small>
                </div>
              </div>

              <div className="profile-metrics">
                <div>
                  <small>SERVIÇOS</small>
                  <strong>52</strong>
                </div>

                <div>
                  <small>CONCLUÍDOS</small>
                  <strong>49</strong>
                </div>

                <div>
                  <small>ESPECIALIDADES</small>
                  <strong>05</strong>
                </div>

                <div>
                  <small>CERTIFICAÇÕES</small>
                  <strong>03</strong>
                </div>
              </div>

              <div className="profile-skills">
                <span>Redes TCP/IP</span>
                <span>Switches</span>
                <span>Wi-Fi</span>
                <span>Hardware</span>
                <span>Windows</span>
              </div>

              <div className="profile-actions">
                <button type="button">
                  Ver perfil completo
                </button>

                <button type="button">
                  <MessageSquareText size={15} />
                  Conversar
                </button>
              </div>
            </div>
          </div>
        </section>

        <section
          className="tools-section"
          id="ferramentas"
        >
          <div className="container tools-layout">
            <div className="tool-interface">
              <div className="tool-interface-top">
                <div>
                  <span className="mono-label">
                    CATÁLOGO TÉCNICO
                  </span>

                  <strong>KIT-0042</strong>
                </div>

                <span className="availability">
                  <span />
                  DISPONÍVEL
                </span>
              </div>

              <div className="tool-product">
                <div className="tool-visual">
                  <Wrench size={38} />
                  <Cable size={26} />
                  <Router size={34} />
                </div>

                <div>
                  <span className="mono-label">
                    MANUTENÇÃO / REDES
                  </span>

                  <h3>Kit técnico essencial</h3>

                  <p>
                    Conjunto para diagnóstico, manutenção e
                    pequenos atendimentos de infraestrutura.
                  </p>
                </div>
              </div>

              <div className="tool-specs">
                <div>
                  <small>DISPONIBILIDADE</small>
                  <strong>05 unidades</strong>
                </div>

                <div>
                  <small>VALOR / DIA</small>
                  <strong>R$ 50,00</strong>
                </div>

                <div>
                  <small>RETIRADA</small>
                  <strong>Presencial</strong>
                </div>
              </div>

              <div className="tool-items">
                <div>
                  <span>01</span>
                  Chaves de precisão
                </div>

                <div>
                  <span>02</span>
                  Cabos e adaptadores
                </div>

                <div>
                  <span>03</span>
                  Testadores e diagnóstico
                </div>
              </div>

              <button className="tool-action" type="button">
                Ver detalhes do kit
                <ArrowRight size={16} />
              </button>
            </div>

            <div className="tools-copy">
              <div className="tools-symbol">
                <PackageOpen size={24} />
              </div>

              <span className="eyebrow">
                DIFERENCIAL TECHHELP
              </span>

              <h2>
                Ferramentas também
                <br />
                fazem parte do serviço.
              </h2>

              <p>
                Profissionais podem utilizar o módulo de aluguel
                para localizar kits e ferramentas adequados ao
                atendimento que precisam realizar.
              </p>

              <div className="tools-benefits">
                <div>
                  <strong>01</strong>
                  <span>
                    Consulta de disponibilidade
                  </span>
                </div>

                <div>
                  <strong>02</strong>
                  <span>
                    Controle de retirada e devolução
                  </span>
                </div>

                <div>
                  <strong>03</strong>
                  <span>
                    Valor calculado por período
                  </span>
                </div>
              </div>

              <button className="outline-button" type="button">
                Explorar ferramentas
                <ArrowRight size={16} />
              </button>
            </div>
          </div>
        </section>

        <section className="closing-section">
          <div className="container closing-panel">
            <div className="closing-main">
              <span className="eyebrow light">
                TECHHELP / READY
              </span>

              <h2>
                O problema é técnico.
                <br />
                A solução também deve ser.
              </h2>

              <p>
                Encontre profissionais especializados ou
                transforme sua experiência em novas oportunidades.
              </p>
            </div>

            <div className="closing-actions">
              <button className="light-button" type="button">
                Preciso de um técnico
                <ArrowRight size={16} />
              </button>

              <button className="transparent-button" type="button">
                Sou profissional de TI
                <ArrowRight size={16} />
              </button>
            </div>
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <div className="container footer-inner">
          <a href="/" className="brand">
            <span className="brand-symbol footer-symbol">
              <Terminal size={16} />
            </span>

            <span className="brand-word">
              Tech<span>Help</span>
            </span>
          </a>

          <p>
            Marketplace especializado em serviços de tecnologia.
          </p>

          <span className="footer-code">
            TH / PLATFORM / 2026
          </span>
        </div>
      </footer>
    </div>
  );
}

export default Home;