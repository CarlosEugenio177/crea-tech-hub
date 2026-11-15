package br.com.mutua.createchhub.space;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "spaces")
public class Space {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 150)
  @Column(nullable = false, length = 150)
  private String name;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private SpaceType type;

  @Positive
  @Column(nullable = false)
  private int capacity;

  @Size(max = 2000)
  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Space() {}

  public Space(String name, SpaceType type, int capacity, String description) {
    update(name, type, capacity, description);
  }

  void update(String name, SpaceType type, int capacity, String description) {
    this.name = name == null ? null : name.strip();
    this.type = type;
    this.capacity = capacity;
    this.description = description == null ? null : description.strip();
  }

  void changeStatus(boolean active) {
    this.active = active;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public SpaceType getType() {
    return type;
  }

  public int getCapacity() {
    return capacity;
  }

  public String getDescription() {
    return description;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
