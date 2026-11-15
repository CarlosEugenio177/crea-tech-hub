package br.com.mutua.createchhub.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "access_audit_events")
public class AccessAuditEvent {

  @Id private UUID id;

  @Column(name = "actor_id")
  private UUID actorId;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(nullable = false, length = 40)
  private String action;

  @Column(nullable = false, length = 1000)
  private String detail;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected AccessAuditEvent() {}

  AccessAuditEvent(UUID actorId, UUID targetId, String action, String detail) {
    id = UUID.randomUUID();
    this.actorId = actorId;
    this.targetId = targetId;
    this.action = action;
    this.detail = detail;
  }

  public UUID getId() {
    return id;
  }

  public UUID getActorId() {
    return actorId;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public String getAction() {
    return action;
  }

  public String getDetail() {
    return detail;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
