package br.com.techhelp.dto;

import br.com.techhelp.model.Aluguel;
import java.util.List;

public record AlugueisAdminResponse(List<Item> content, int number, int totalPages) {
    public record Item(Aluguel aluguel, String nomeUsuario) {}
}
