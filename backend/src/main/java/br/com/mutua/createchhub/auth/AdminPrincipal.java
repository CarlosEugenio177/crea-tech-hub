package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.UserAccount;
import java.io.Serial;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public final class AdminPrincipal implements UserDetails, CredentialsContainer {

  @Serial private static final long serialVersionUID = 1L;

  private final UUID accountId;
  private final long securityVersion;
  private final User user;

  public AdminPrincipal(UserAccount account) {
    accountId = account.getId();
    securityVersion = account.getSecurityVersion();
    var authorities =
        Stream.concat(
                Stream.of("ROLE_" + account.getAccessRole().name()),
                account.getPermissions().stream()
                    .map(permission -> Objects.requireNonNull(permission).name()))
            .map(SimpleGrantedAuthority::new)
            .toList();
    user =
        new User(
            account.getEmail(),
            account.getPasswordHash(),
            account.isActive(),
            true,
            true,
            true,
            authorities);
  }

  public UUID getAccountId() {
    return accountId;
  }

  public long getSecurityVersion() {
    return securityVersion;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getAuthorities();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  @Override
  public void eraseCredentials() {
    user.eraseCredentials();
  }
}
