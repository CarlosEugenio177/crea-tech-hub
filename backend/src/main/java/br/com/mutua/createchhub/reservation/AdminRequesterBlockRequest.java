package br.com.mutua.createchhub.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminRequesterBlockRequest(
    @NotNull @Positive(message = "Informe pelo menos um dia de bloqueio.") Integer durationDays) {}
