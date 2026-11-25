package br.com.mutua.createchhub.reservation;

import jakarta.validation.constraints.NotNull;

public record ReservationCancellationRequest(@NotNull Boolean confirmLateCancellation) {}
