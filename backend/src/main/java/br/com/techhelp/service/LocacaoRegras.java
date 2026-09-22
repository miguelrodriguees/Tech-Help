package br.com.techhelp.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public final class LocacaoRegras {
    private LocacaoRegras() {}
    // Diária de 24 horas; qualquer fração conta como mais uma diária.
    public static long diarias(LocalDateTime inicio, LocalDateTime fim, LocalDateTime agora) {
        if (inicio==null || fim==null || inicio.isBefore(agora) || !fim.isAfter(inicio))
            throw new IllegalArgumentException("Escolha uma retirada futura e uma devolução depois dela");
        Duration periodo=Duration.between(inicio,fim);
        if (periodo.compareTo(Duration.ofDays(30))>0) throw new IllegalArgumentException("O período máximo é de 30 dias");
        long dias=periodo.toDays();
        return periodo.equals(Duration.ofDays(dias))?dias:dias+1;
    }
    public static BigDecimal total(BigDecimal aluguel, BigDecimal entrega, BigDecimal coleta) {
        if (entrega==null || coleta==null) return null;
        return aluguel.add(entrega).add(coleta);
    }
}
