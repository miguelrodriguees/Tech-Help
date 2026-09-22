package br.com.techhelp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class LocacaoDtos {
    private LocacaoDtos() {}
    public record Item(@NotBlank @Pattern(regexp="FERRAMENTA|KIT") String tipo,
                       @NotNull @Positive Long id, @Min(1) @Max(20) int quantidade) {}
    public record Pedido(@NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") String chave,
                         @NotNull LocalDateTime retirada, @NotNull LocalDateTime devolucao,
                         @NotBlank @Pattern(regexp="RETIRADA|ENTREGA") String recebimento,
                         @NotBlank @Pattern(regexp="NO_TECHHELP|COLETA") String retorno,
                         @Size(max=600) String endereco,
                         @NotEmpty @Size(max=20) List<@NotNull @Valid Item> itens) {}
    public record Taxas(@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal entrega,
                        @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal coleta,
                        @NotBlank @Size(max=1000) String observacao) {}
    public record Ferramenta(@NotBlank @Size(max=140) String nome, @Size(max=3000) String descricao,
                            @Min(1) @Max(10000) int quantidade,
                            @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal diaria) {}
    public record Componente(@NotNull @Positive Long id, @Min(1) @Max(1000) int quantidade) {}
    public record Kit(@NotBlank @Size(max=140) String nome, @Size(max=3000) String descricao,
                      @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal diaria,
                      @NotEmpty @Size(max=30) List<@NotNull @Valid Componente> componentes) {}
    public record Estado(@NotBlank @Pattern(regexp="DISPONIVEL|MANUTENCAO|INATIVA") String status) {}
}
