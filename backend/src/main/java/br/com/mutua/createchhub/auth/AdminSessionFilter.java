package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.shared.error.SecurityProblemWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

public class AdminSessionFilter extends OncePerRequestFilter {

  private static final Set<String> PASSWORD_PATHS =
      Set.of(
          "/api/v1/admin/auth/me",
          "/api/v1/admin/auth/csrf",
          "/api/v1/admin/auth/logout",
          "/api/v1/admin/auth/login",
          "/api/v1/admin/auth/password");
  private final AdminSessionValidator sessions;
  private final SecurityProblemWriter problems;

  public AdminSessionFilter(AdminSessionValidator sessions, SecurityProblemWriter problems) {
    this.sessions = sessions;
    this.problems = problems;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().startsWith("/api/v1/admin/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof AdminPrincipal principal) {
      var state = sessions.check(principal);
      if (state == AdminSessionValidator.SessionState.REVOKED) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        problems.write(
            request,
            response,
            HttpStatus.UNAUTHORIZED,
            "Sua sessão foi encerrada. Entre novamente.");
        return;
      }
      if (state == AdminSessionValidator.SessionState.PASSWORD_CHANGE_REQUIRED
          && !PASSWORD_PATHS.contains(request.getServletPath())) {
        problems.write(
            request, response, HttpStatus.FORBIDDEN, "Altere sua senha inicial para continuar.");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
