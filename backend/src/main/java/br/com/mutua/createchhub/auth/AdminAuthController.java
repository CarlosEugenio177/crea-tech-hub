package br.com.mutua.createchhub.auth;

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

	@GetMapping("/csrf")
	public ResponseEntity<CsrfTokenResponse> csrf(CsrfToken token) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore())
				.body(new CsrfTokenResponse(token.getHeaderName(), token.getParameterName(), token.getToken()));
	}

	@GetMapping("/me")
	public ResponseEntity<AdminAccountResponse> current(Authentication authentication) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore())
				.body(accounts.current(authentication.getName()));
	}
}
