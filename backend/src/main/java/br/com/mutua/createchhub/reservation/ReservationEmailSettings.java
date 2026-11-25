package br.com.mutua.createchhub.reservation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.reservation-email")
record ReservationEmailSettings(
    boolean enabled, String apiKey, String from, String publicOrigin, String linkSecret) {}
