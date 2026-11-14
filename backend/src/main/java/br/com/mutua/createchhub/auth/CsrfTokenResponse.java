package br.com.mutua.createchhub.auth;

public record CsrfTokenResponse(String headerName, String parameterName, String token) {}
