package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AgendarServicoRequest(@NotNull LocalDateTime dataAgendada) {}
