package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spaces")
public class PublicSpaceController {

  private final SpaceService spaces;

  public PublicSpaceController(SpaceService spaces) {
    this.spaces = spaces;
  }

  @Operation(
      operationId = "listPublicSpaces",
      summary = "Listar espaços disponíveis",
      security = {})
  @GetMapping
  public PublicSpacePageResponse list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return PublicSpacePageResponse.from(spaces.listPublic(page, size));
  }
}
