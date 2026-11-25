package br.com.mutua.createchhub.block;

import br.com.mutua.createchhub.shared.error.InvalidRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "space_blocks")
public class SpaceBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "space_id", nullable = false, updatable = false)
  private UUID spaceId;

  @Column(name = "series_id", updatable = false)
  private UUID seriesId;

  @Column(name = "starts_at", nullable = false, updatable = false)
  private LocalDateTime startsAt;

  @Column(name = "ends_at", nullable = false, updatable = false)
  private LocalDateTime endsAt;

  @Column(nullable = false, length = 500, updatable = false)
  private String reason;

  @Column(name = "created_by", nullable = false, updatable = false)
  private UUID createdBy;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "cancelled_by")
  private UUID cancelledBy;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  protected SpaceBlock() {}

  static SpaceBlock create(
      UUID spaceId,
      LocalDateTime start,
      LocalDateTime end,
      String reason,
      UUID actor,
      UUID seriesId) {
    if (spaceId == null
        || actor == null
        || start == null
        || end == null
        || !end.isAfter(start)
        || start.getSecond() != 0
        || start.getNano() != 0
        || end.getSecond() != 0
        || end.getNano() != 0
        || reason == null
        || reason.isBlank()
        || reason.length() > 500) {
      throw new InvalidRequestException(
          "Informe um espaço, motivo e intervalo válido com precisão de minutos.");
    }
    var block = new SpaceBlock();
    block.spaceId = spaceId;
    block.seriesId = seriesId;
    block.startsAt = start;
    block.endsAt = end;
    block.reason = reason.strip();
    block.createdBy = actor;
    return block;
  }

  void cancel(UUID actor) {
    if (cancelledAt == null) {
      cancelledBy = actor;
      cancelledAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getSpaceId() {
    return spaceId;
  }

  public UUID getSeriesId() {
    return seriesId;
  }

  public LocalDateTime getStartsAt() {
    return startsAt;
  }

  public LocalDateTime getEndsAt() {
    return endsAt;
  }

  public String getReason() {
    return reason;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public UUID getCancelledBy() {
    return cancelledBy;
  }

  public Instant getCancelledAt() {
    return cancelledAt;
  }
}
