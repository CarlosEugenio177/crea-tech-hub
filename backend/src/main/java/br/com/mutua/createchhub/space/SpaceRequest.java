package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.net.URI;

public record SpaceRequest(
    @NotBlank(message = "Informe o nome do espaço.")
        @Size(max = 150, message = "O nome deve ter até 150 caracteres.")
        String name,
    @NotNull(message = "Informe o tipo do espaço.") SpaceType type,
    @NotNull(message = "Informe a capacidade do espaço.")
        @Positive(message = "A capacidade deve ser maior que zero.")
        @Schema(description = "Inteiro positivo; estações individuais exigem capacidade 1.")
        Integer capacity,
    @Schema(types = {"string", "null"})
        @Size(max = 2000, message = "A descrição deve ter até 2000 caracteres.")
        String description,
    @Size(max = 2048)
        @Schema(description = "URL HTTPS pública da foto. No PUT, valor vazio remove a foto.")
        String photoUrl) {

  @AssertTrue(message = "Informe um link HTTPS válido para a foto.")
  @Schema(hidden = true)
  public boolean isPhotoUrlValid() {
    if (photoUrl == null || photoUrl.isBlank()) return true;
    try {
      var url = URI.create(photoUrl.strip());
      return "https".equalsIgnoreCase(url.getScheme())
          && url.getHost() != null
          && url.getUserInfo() == null;
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  @AssertTrue(message = "Uma estação individual deve ter capacidade para uma pessoa.")
  @Schema(hidden = true)
  public boolean isCapacityCompatible() {
    return type != SpaceType.WORKSTATION || capacity == null || capacity == 1;
  }
}
