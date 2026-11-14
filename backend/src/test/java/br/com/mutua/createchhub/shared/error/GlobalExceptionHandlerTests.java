package br.com.mutua.createchhub.shared.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ErrorTestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTests {

  @Autowired private MockMvc mvc;

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "not-found | 404 | Usuário não encontrado.",
        "conflict | 409 | O horário solicitado já está ocupado.",
        "authentication | 401 | Autenticação necessária ou inválida.",
        "forbidden | 403 | Você não tem permissão para esta operação."
      })
  void returnsProblemDetailsForExpectedExceptions(String path, int status, String detail)
      throws Exception {
    mvc.perform(get("/test/errors/" + path))
        .andExpect(status().is(status))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").isNotEmpty())
        .andExpect(jsonPath("$.status").value(status))
        .andExpect(jsonPath("$.detail").value(detail))
        .andExpect(jsonPath("$.instance").value("/test/errors/" + path));
  }

  @Test
  void returnsFieldValidationErrorsWithoutRejectedValues() throws Exception {
    mvc.perform(
            post("/test/errors/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.errors[0].field").value("name"))
        .andExpect(jsonPath("$.errors[0].message").value("O nome é obrigatório."))
        .andExpect(jsonPath("$.errors[0].rejectedValue").doesNotExist());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{", ""})
  void returnsBadRequestForMalformedOrMissingBody(String body) throws Exception {
    mvc.perform(
            post("/test/errors/validation").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(
            jsonPath("$.detail")
                .value("O corpo da requisição está ausente ou contém JSON inválido."));
  }

  @Test
  void preservesFrameworkNotFoundResponse() throws Exception {
    mvc.perform(get("/test/errors/missing"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void preservesAllowedMethodsHeader() throws Exception {
    mvc.perform(put("/test/errors/not-found"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().string("Allow", containsString("GET")))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @ParameterizedTest
  @CsvSource({"unexpected,500", "server-error,503"})
  void hidesInternalDetailsInServerErrors(String path, int status) throws Exception {
    var response =
        mvc.perform(get("/test/errors/" + path))
            .andExpect(status().is(status))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(
                jsonPath("$.detail").value("Ocorreu um erro interno. Tente novamente mais tarde."))
            .andReturn()
            .getResponse();

    assertThat(response.getContentAsString(StandardCharsets.UTF_8))
        .doesNotContain("database.internal", "SQLSTATE", "exception", "trace");
  }
}
