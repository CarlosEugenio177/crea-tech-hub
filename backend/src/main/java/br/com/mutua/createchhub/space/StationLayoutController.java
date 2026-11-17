package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('MANAGE_SPACES')")
@RequestMapping("/api/v1/admin/spaces")
public class StationLayoutController {

  private final StationLayoutService layout;

  public StationLayoutController(StationLayoutService layout) {
    this.layout = layout;
  }

  @Operation(operationId = "listAdminRoomStations", summary = "Listar estações de uma sala")
  @GetMapping("/{roomId}/stations")
  public SpacePageResponse list(
      @PathVariable UUID roomId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return layout.list(roomId, false, page, size);
  }

  @Operation(
      operationId = "placeStation",
      summary = "Vincular e posicionar estação no mapa de uma sala")
  @PutMapping("/{id}/station-placement")
  public SpaceResponse place(
      @PathVariable UUID id, @Valid @RequestBody StationPlacementRequest request) {
    return layout.place(id, request);
  }

  @Operation(operationId = "createStationsBatch", summary = "Gerar estações numeradas em uma sala")
  @PostMapping("/{roomId}/stations/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<SpaceResponse> generate(
      @PathVariable UUID roomId, @Valid @RequestBody StationBatchRequest request) {
    return layout.generate(roomId, request);
  }

  @Operation(operationId = "saveStationPositions", summary = "Salvar posições do mapa em conjunto")
  @PutMapping("/{roomId}/station-positions")
  public List<SpaceResponse> savePositions(
      @PathVariable UUID roomId, @Valid @RequestBody StationPositionsRequest request) {
    return layout.savePositions(roomId, request);
  }
}
