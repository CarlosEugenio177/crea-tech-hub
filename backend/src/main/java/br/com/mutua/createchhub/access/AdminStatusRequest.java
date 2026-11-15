package br.com.mutua.createchhub.access;

import jakarta.validation.constraints.NotNull;

public record AdminStatusRequest(@NotNull Boolean active) {}
