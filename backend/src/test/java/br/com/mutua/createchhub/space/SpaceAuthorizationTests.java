package br.com.mutua.createchhub.space;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.mutua.createchhub.support.PostgresTestConfiguration;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class SpaceAuthorizationTests {

  @Autowired private MockMvc mvc;

  @ParameterizedTest
  @ValueSource(strings = {"", "/00000000-0000-0000-0000-000000000001"})
  void deniesAnAuthenticatedUserWithoutTheAdminRole(String suffix) throws Exception {
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(
            "user@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    var session = new MockHttpSession();
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

    mvc.perform(get("/api/v1/admin/spaces" + suffix).session(session))
        .andExpect(status().isForbidden());
  }
}
