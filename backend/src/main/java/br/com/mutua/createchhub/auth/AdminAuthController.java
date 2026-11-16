package br.com.mutua.createchhub.auth;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

  private final AdminAccountService accounts;

  public AdminAuthController(AdminAccountService accounts) {
    this.accounts = accounts;
  }

  @Operation(operationId = "getCsrfToken", summary = "Obter CSRF")
  @GetMapping("/csrf")
  public ResponseEntity<CsrfTokenResponse> csrf(
      @io.swagger.v3.oas.annotations.Parameter(hidden = true) CsrfToken token) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(
            new CsrfTokenResponse(
                token.getHeaderName(), token.getParameterName(), token.getToken()));
  }

  @Operation(operationId = "getCurrentAdmin", summary = "Consultar a sessão")
  @GetMapping("/me")
  public ResponseEntity<AdminAccountResponse> current(Authentication authentication) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(accounts.current(authentication.getName()));
  }
}
