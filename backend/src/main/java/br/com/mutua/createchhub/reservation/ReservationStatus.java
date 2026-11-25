package br.com.mutua.createchhub.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum ReservationStatus {
  PENDING,
  CONFIRMED,
  REJECTED,
  CANCELLED
}
