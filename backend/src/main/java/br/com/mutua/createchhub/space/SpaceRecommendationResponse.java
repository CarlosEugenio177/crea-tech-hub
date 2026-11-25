package br.com.mutua.createchhub.space;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "SpaceRecommendation",
    requiredProperties = {"status", "spaces"})
public record SpaceRecommendationResponse(
    SpaceAvailability.Status status,
    PublicSpaceResponse recommended,
    List<PublicSpaceResponse> spaces,
    String message) {

  private static final String CLOSED_MESSAGE =
      "O Hub está fechado nesse período. Ajuste a data ou o horário.";
  private static final String UNAVAILABLE_MESSAGE =
      "Nenhum ambiente disponível atende essa quantidade de pessoas nesse período. "
          + "Ajuste a data, o horário ou o número de pessoas.";

  public SpaceRecommendationResponse {
    spaces = List.copyOf(spaces);
  }

  static SpaceRecommendationResponse available(List<PublicSpaceResponse> spaces) {
    return new SpaceRecommendationResponse(
        SpaceAvailability.Status.AVAILABLE, spaces.getFirst(), spaces, null);
  }

  static SpaceRecommendationResponse closed() {
    return new SpaceRecommendationResponse(
        SpaceAvailability.Status.CLOSED, null, List.of(), CLOSED_MESSAGE);
  }

  static SpaceRecommendationResponse unavailable() {
    return new SpaceRecommendationResponse(
        SpaceAvailability.Status.UNAVAILABLE, null, List.of(), UNAVAILABLE_MESSAGE);
  }
}
