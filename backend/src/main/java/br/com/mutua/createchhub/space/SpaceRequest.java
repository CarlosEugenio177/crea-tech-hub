package br.com.mutua.createchhub.space;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SpaceRequest(
    @NotBlank(message = "Informe o nome do espaço.")
        @Size(max = 150, message = "O nome deve ter até 150 caracteres.")
        String name,
    @NotNull(message = "Informe o tipo do espaço.") SpaceType type,
    @NotNull(message = "Informe a capacidade do espaço.")
        @Positive(message = "A capacidade deve ser maior que zero.")
        Integer capacity,
    @Size(max = 2000, message = "A descrição deve ter até 2000 caracteres.") String description) {

  @AssertTrue(message = "Uma estação individual deve ter capacidade para uma pessoa.")
  public boolean isCapacityCompatible() {
    return type != SpaceType.WORKSTATION || capacity == null || capacity == 1;
  }
}
