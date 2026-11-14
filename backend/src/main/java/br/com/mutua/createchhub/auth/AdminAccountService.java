package br.com.mutua.createchhub.auth;

import br.com.mutua.createchhub.user.AccessRole;
import br.com.mutua.createchhub.user.UserAccount;
import br.com.mutua.createchhub.user.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

	private final UserAccountRepository repository;

	public AdminAccountService(UserAccountRepository repository) {
		this.repository = repository;
	}

	public AdminAccountResponse current(String email) {
		return repository.findByEmailAndAccessRole(email, AccessRole.ADMIN)
				.filter(UserAccount::isActive)
				.map(AdminAccountResponse::from)
				.orElseThrow(() -> new BadCredentialsException("Conta administrativa indisponível."));
	}
}
