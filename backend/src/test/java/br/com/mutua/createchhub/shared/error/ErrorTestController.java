package br.com.mutua.createchhub.shared.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/test/errors")
class ErrorTestController {

	@GetMapping("/not-found")
	void notFound() {
		throw new ResourceNotFoundException("Usuário não encontrado.");
	}

	@GetMapping("/conflict")
	void conflict() {
		throw new ResourceConflictException("O horário solicitado já está ocupado.");
	}

	@GetMapping("/authentication")
	void authentication() {
		throw new BadCredentialsException("Internal authentication details");
	}

	@GetMapping("/forbidden")
	void forbidden() {
		throw new AccessDeniedException("Internal authorization details");
	}

	@GetMapping("/unexpected")
	void unexpected() {
		throw new IllegalStateException("SQLSTATE 23505: database.internal");
	}

	@GetMapping("/server-error")
	void serverError() {
		throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "database.internal unavailable");
	}

	@PostMapping("/validation")
	void validation(@Valid @RequestBody TestRequest request) {
	}

	record TestRequest(@NotBlank(message = "O nome é obrigatório.") String name) {
	}
}
