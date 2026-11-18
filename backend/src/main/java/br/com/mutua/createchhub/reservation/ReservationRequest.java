package br.com.mutua.createchhub.reservation;

import br.com.mutua.createchhub.block.SpaceBlockRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.hibernate.validator.constraints.br.CPF;

public record ReservationRequest(
    @NotNull UUID spaceId,
    UUID stationId,
    @NotBlank @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String startsAt,
    @NotBlank @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String endsAt,
    @NotNull @Valid Requester requester,
    @NotNull @AssertTrue(message = "Aceite as regras de uso para continuar.")
        Boolean rulesAccepted) {

  public record Requester(
      @NotBlank @Pattern(regexp = "[0-9]{11}") @CPF(message = "Informe um CPF válido.") String cpf,
      @NotBlank @Size(max = 150) String fullName,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Pattern(regexp = "[0-9]{10,11}") String whatsapp,
      @NotNull Profile profile,
      @NotNull Boolean onBehalfOfOthers,
      @NotNull @Size(max = 500) String reason,
      @NotNull @Size(max = 1000) String notes) {

    @AssertTrue(message = "Informe o motivo da reserva.")
    @Schema(hidden = true)
    public boolean isReasonValid() {
      return (profile != Profile.EXTERNAL && !Boolean.TRUE.equals(onBehalfOfOthers))
          || (reason != null && !reason.isBlank());
    }
  }

  @Schema(enumAsRef = true)
  public enum Profile {
    MUTUALIST,
    EXTERNAL
  }
}
