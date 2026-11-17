package br.com.mutua.createchhub.space;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StationBatchRequest(
    @NotNull @Min(1) @Max(100) Integer count, @NotNull @Min(1) @Max(100) Integer columns) {}
