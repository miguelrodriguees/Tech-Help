package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarSolicitacaoAnexoRequest;
import br.com.techhelp.model.SolicitacaoAnexo;
import br.com.techhelp.repository.SolicitacaoAnexoRepository;
import br.com.techhelp.repository.SolicitacaoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class SolicitacaoAnexoService {

    private final SolicitacaoAnexoRepository anexoRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public SolicitacaoAnexoService(
            SolicitacaoAnexoRepository anexoRepository,
            SolicitacaoRepository solicitacaoRepository
    ) {
        this.anexoRepository = anexoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @PreAuthorize("@acesso.donoSolicitacao(#idSolicitacao)")
    public SolicitacaoAnexo adicionar(
            Long idSolicitacao,
            CriarSolicitacaoAnexoRequest request
    ) {

        if (!solicitacaoRepository.existsById(idSolicitacao)) {
            throw new NoSuchElementException(
                    "Solicitação não encontrada"
            );
        }

        SolicitacaoAnexo anexo =
                new SolicitacaoAnexo();

        anexo.setIdSolicitacao(idSolicitacao);
        anexo.setNomeOriginal(request.nomeOriginal());
        anexo.setArquivoUrl(request.arquivoUrl());
        anexo.setTipoArquivo(request.tipoArquivo());
        anexo.setTamanhoBytes(request.tamanhoBytes());

        return anexoRepository.save(anexo);
    }

    @PreAuthorize("@acesso.lerSolicitacao(#idSolicitacao)")
    public List<SolicitacaoAnexo> listar(
            Long idSolicitacao
    ) {

        if (!solicitacaoRepository.existsById(idSolicitacao)) {
            throw new NoSuchElementException(
                    "Solicitação não encontrada"
            );
        }

        return anexoRepository
                .findByIdSolicitacaoOrderByDataUploadDesc(
                        idSolicitacao
                );
    }

    @PreAuthorize("@acesso.lerAnexo(#idAnexo)")
    public SolicitacaoAnexo buscarPorId(
            Long idAnexo
    ) {

        return anexoRepository
                .findById(idAnexo)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Anexo não encontrado"
                        ));
    }

    @PreAuthorize("@acesso.donoSolicitacao(#idSolicitacao)")
    public void excluir(
            Long idSolicitacao,
            Long idAnexo
    ) {

        SolicitacaoAnexo anexo =
                buscarPorId(idAnexo);

        if (!anexo.getIdSolicitacao()
                .equals(idSolicitacao)) {

            throw new IllegalArgumentException(
                    "Este anexo não pertence à solicitação informada"
            );
        }

        anexoRepository.delete(anexo);
    }
}
