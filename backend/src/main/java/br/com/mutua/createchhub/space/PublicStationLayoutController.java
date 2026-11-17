package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spaces/{roomId}/stations")
public class PublicStationLayoutController {

  private final StationLayoutService layout;

  public PublicStationLayoutController(StationLayoutService layout) {
    this.layout = layout;
  }

  @Operation(operationId = "listRoomStations", summary = "Consultar estações públicas de uma sala")
  @GetMapping
  public SpacePageResponse list(
      @PathVariable UUID roomId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return layout.list(roomId, true, page, size);
  }
}
