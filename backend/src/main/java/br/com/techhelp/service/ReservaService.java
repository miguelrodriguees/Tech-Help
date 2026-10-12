package br.com.techhelp.service;

import br.com.techhelp.dto.AdicionarItemAluguelRequest;
import br.com.techhelp.dto.CriarAluguelRequest;
import br.com.techhelp.dto.ReservarKitRequest;
import br.com.techhelp.model.Aluguel;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaService {
    private final AluguelService alugueis;
    private final AluguelItemService itens;

    public ReservaService(AluguelService alugueis, AluguelItemService itens) {
        this.alugueis = alugueis;
        this.itens = itens;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("@acesso.mesmoUsuario(#request.idUsuario())")
    public Aluguel reservar(ReservarKitRequest request) {
        if (!request.dataPrevistaRetirada().isAfter(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new IllegalArgumentException("A retirada precisa ser futura, no horário de Brasília");
        }
        Aluguel aluguel = alugueis.criar(new CriarAluguelRequest(request.idUsuario(),
                request.dataPrevistaRetirada(), request.dataPrevistaDevolucao()));
        // Ambos participam da mesma transação: falha de estoque desfaz também o aluguel.
        itens.adicionar(aluguel.getIdAluguel(), new AdicionarItemAluguelRequest(request.idKit(), request.quantidade()));
        return aluguel;
    }
}
