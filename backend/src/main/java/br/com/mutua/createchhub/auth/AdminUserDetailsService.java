package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccountRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminUserDetailsService implements UserDetailsService {

  private final UserAccountRepository repository;

  public AdminUserDetailsService(UserAccountRepository repository) {
    this.repository = repository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    var account =
        repository
            .findByEmailAndAccessRole(email.strip().toLowerCase(Locale.ROOT), AccessRole.ADMIN)
            .orElseThrow(
                () -> new UsernameNotFoundException("Conta administrativa não encontrada."));
    return User.withUsername(account.getEmail())
        .password(account.getPasswordHash())
        .roles(account.getAccessRole().name())
        .disabled(!account.isActive())
        .build();
  }
}
