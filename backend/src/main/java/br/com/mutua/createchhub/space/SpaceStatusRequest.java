package br.com.mutua.createchhub.space;

import jakarta.validation.constraints.NotNull;

public record SpaceStatusRequest(
    @NotNull(message = "Informe se o espaço deve ficar ativo.") Boolean active) {}
