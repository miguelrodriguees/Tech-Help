package br.com.techhelp.service;

import br.com.techhelp.dto.CriarAvaliacaoRequest;
import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class AtendimentoService {
    private final ServicoRepository servicos;
    private final ServicoService fluxo;
    private final AvaliacaoService avaliacoes;
    private final ClienteRepository clientes;
    private final TecnicoRepository tecnicos;
    public AtendimentoService(ServicoRepository servicos, ServicoService fluxo, AvaliacaoService avaliacoes,
                              ClienteRepository clientes, TecnicoRepository tecnicos) {
        this.servicos=servicos;this.fluxo=fluxo;this.avaliacoes=avaliacoes;this.clientes=clientes;this.tecnicos=tecnicos;
    }
    private Servico proprio(Long id, Long participante, boolean tecnico, boolean bloquear) {
        var servico=(bloquear?servicos.buscarParaAtualizar(id):servicos.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!(tecnico?servico.getIdTecnico():servico.getIdCliente()).equals(participante))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return servico;
    }
    @Transactional
    public Servico iniciar(Long id, Long tecnico) {
        var s=proprio(id,tecnico,true,true);
        if ("EM_ANDAMENTO".equals(s.getStatus())) return s;
        return fluxo.iniciarServico(id);
    }
    @Transactional
    public Servico concluir(Long id, Long cliente) {
        var s=proprio(id,cliente,false,true);
        if ("CONCLUIDO".equals(s.getStatus())) return s;
        return fluxo.concluirServico(id);
    }
    public List<Avaliacao> consultarAvaliacoes(Long id, Long participante, boolean tecnico) {
        var s=proprio(id,participante,tecnico,false);
        Long autor=clientes.findById(s.getIdCliente()).orElseThrow().getIdUsuario();
        return avaliacoes.listarPorServico(id).stream().filter(a -> a.getIdAvaliador().equals(autor)).toList();
    }
    @Transactional
    public Avaliacao avaliar(Long id, Long cliente, Integer nota, String comentario) {
        var s=proprio(id,cliente,false,true);
        Long autor=clientes.findById(cliente).orElseThrow().getIdUsuario();
        Long destino=tecnicos.findById(s.getIdTecnico()).orElseThrow().getIdUsuario();
        return avaliacoes.criar(new CriarAvaliacaoRequest(id,autor,destino,nota,comentario));
    }
}
