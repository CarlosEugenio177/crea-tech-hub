package br.com.mutua.createchhub.reservation;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class ReservationEmailLinks {
  private final URI origin;
  private final SecretKeySpec key;

  ReservationEmailLinks(ReservationEmailSettings settings) {
    origin = parseOrigin(settings.publicOrigin());
    key = readKey(settings.linkSecret());
  }

  String token(UUID id) {
    try {
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(key);
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(
              mac.doFinal(("reservation-email:" + id).getBytes(StandardCharsets.US_ASCII)));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("HMAC indisponível.", exception);
    }
  }

  URI url(String token) {
    return origin.resolve("/acompanhar#" + token);
  }

  private static URI parseOrigin(String value) {
    try {
      var uri = URI.create(value);
      if ("https".equals(uri.getScheme())
          && uri.getHost() != null
          && uri.getUserInfo() == null
          && uri.getQuery() == null
          && uri.getFragment() == null
          && (uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
        return uri;
      }
    } catch (IllegalArgumentException exception) {
      // O valor da configuração pode conter credenciais.
    }
    throw new IllegalArgumentException(
        "RESERVATION_EMAIL_PUBLIC_ORIGIN deve ser uma origem HTTPS.");
  }

  private static SecretKeySpec readKey(String value) {
    try {
      var decoded = Base64.getDecoder().decode(value);
      if (decoded.length >= 32) return new SecretKeySpec(decoded, "HmacSHA256");
    } catch (IllegalArgumentException exception) {
      // A mensagem do decodificador pode conter parte do segredo.
    }
    throw new IllegalArgumentException(
        "RESERVATION_EMAIL_LINK_SECRET exige ao menos 32 bytes em Base64.");
  }
}
