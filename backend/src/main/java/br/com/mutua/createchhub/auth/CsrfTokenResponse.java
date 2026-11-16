package br.com.mutua.createchhub.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "CsrfToken",
    requiredProperties = {"headerName", "parameterName", "token"})
public record CsrfTokenResponse(String headerName, String parameterName, String token) {}
