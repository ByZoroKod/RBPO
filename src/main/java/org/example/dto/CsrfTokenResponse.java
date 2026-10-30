package org.example.dto;

public record CsrfTokenResponse(String headerName, String parameterName, String token) {
}
