package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarCertificacaoRequest;
import br.com.techhelp.model.Certificacao;
import br.com.techhelp.repository.CertificacaoRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class CertificacaoService {

    private final CertificacaoRepository certificacaoRepository;
    private final TecnicoRepository tecnicoRepository;

    public CertificacaoService(
            CertificacaoRepository certificacaoRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.certificacaoRepository = certificacaoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @PreAuthorize("@acesso.tecnico(#request.idTecnico())")
    public Certificacao criar(
            CriarCertificacaoRequest request
    ) {

        if (!tecnicoRepository.existsById(request.idTecnico())) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        if (request.dataEmissao() != null
                && request.dataValidade() != null
                && request.dataValidade()
                        .isBefore(request.dataEmissao())) {

            throw new IllegalArgumentException(
                    "A data de validade não pode ser anterior à data de emissão"
            );
        }

        if (request.codigoCredencial() != null
                && !request.codigoCredencial().isBlank()
                && certificacaoRepository
                .existsByIdTecnicoAndCodigoCredencial(
                        request.idTecnico(),
                        request.codigoCredencial()
                )) {

            throw new IllegalArgumentException(
                    "Este técnico já possui uma certificação com esse código"
            );
        }

        Certificacao certificacao = new Certificacao();

        certificacao.setIdTecnico(request.idTecnico());
        certificacao.setNome(request.nome());
        certificacao.setInstituicao(request.instituicao());
        certificacao.setCodigoCredencial(
                request.codigoCredencial()
        );
        certificacao.setArquivoUrl(request.arquivoUrl());
        certificacao.setUrlCredencial(
                request.urlCredencial()
        );
        certificacao.setDataEmissao(request.dataEmissao());
        certificacao.setDataValidade(request.dataValidade());
        certificacao.setStatusVerificacao("PENDENTE");

        return certificacaoRepository.save(certificacao);
    }

    @PreAuthorize("permitAll()")
    public List<Certificacao> listarPorTecnico(
            Long idTecnico
    ) {

        if (!tecnicoRepository.existsById(idTecnico)) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        return certificacaoRepository
                .findByIdTecnicoOrderByDataCadastroDesc(
                        idTecnico
                );
    }

    @PreAuthorize("@acesso.certificacao(#idCertificacao)")
    public void excluir(Long idCertificacao) {

        Certificacao certificacao =
                certificacaoRepository
                        .findById(idCertificacao)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Certificação não encontrada"
                                ));

        certificacaoRepository.delete(certificacao);
    }
}
