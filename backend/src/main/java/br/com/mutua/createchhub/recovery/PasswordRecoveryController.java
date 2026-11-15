package br.com.mutua.createchhub.recovery;

import br.com.mutua.createchhub.auth.AdminPasswordService;
import br.com.mutua.createchhub.auth.AdminPrincipal;
import br.com.mutua.createchhub.auth.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth/password")
public class PasswordRecoveryController {

  private final PasswordRecoveryService recovery;
  private final PasswordResetService tokens;
  private final AdminPasswordService passwords;
  private final RecoveryRequestLimiter limiter;

  public PasswordRecoveryController(
      PasswordRecoveryService recovery,
      PasswordResetService tokens,
      AdminPasswordService passwords,
      RecoveryRequestLimiter limiter) {
    this.recovery = recovery;
    this.tokens = tokens;
    this.passwords = passwords;
    this.limiter = limiter;
  }

  @PostMapping
  public ResponseEntity<Void> change(
      @AuthenticationPrincipal AdminPrincipal principal,
      @Valid @RequestBody PasswordChangeRequest request) {
    passwords.change(principal.getAccountId(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/forgot")
  public ResponseEntity<Void> forgot(
      @Valid @RequestBody ForgotRequest request, HttpServletRequest http) {
    limiter.check(http.getRemoteAddr());
    recovery.request(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/reset")
  public ResponseEntity<Void> reset(
      @Valid @RequestBody PasswordResetRequest request, HttpServletRequest http) {
    limiter.check(http.getRemoteAddr());
    tokens.reset(request);
    return ResponseEntity.noContent().build();
  }

  public record ForgotRequest(@NotBlank @Email @Size(max = 254) String email) {}
}
