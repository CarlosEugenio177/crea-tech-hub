package br.com.mutua.createchhub.space;

import jakarta.validation.constraints.NotNull;

public record BookingModeRequest(@NotNull BookingMode bookingMode) {}
