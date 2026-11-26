package br.com.mutua.createchhub.reservation;

import jakarta.validation.constraints.NotNull;

public record AdminReservationAttendanceRequest(@NotNull Boolean noShow) {}
