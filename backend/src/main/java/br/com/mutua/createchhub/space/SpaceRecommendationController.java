package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.block.SpaceBlockRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpaceRecommendationController {
  private final SpaceRecommendationService recommendations;

  public SpaceRecommendationController(SpaceRecommendationService recommendations) {
    this.recommendations = recommendations;
  }

  @Operation(
      operationId = "recommendSpaces",
      summary = "Recomendar ambientes disponíveis por número de participantes")
  @GetMapping("/api/v1/spaces/recommendations")
  public ResponseEntity<SpaceRecommendationResponse> recommend(
      @RequestParam @Min(1) int participants,
      @RequestParam @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String startsAt,
      @RequestParam @Pattern(regexp = SpaceBlockRequest.TIMESTAMP_PATTERN) String endsAt) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(recommendations.recommend(participants, startsAt, endsAt));
  }
}
