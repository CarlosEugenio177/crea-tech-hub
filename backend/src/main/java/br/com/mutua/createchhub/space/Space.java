package br.com.mutua.createchhub.space;

import br.com.mutua.createchhub.shared.error.ResourceConflictException;
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
import java.math.BigDecimal;
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

  @Column(name = "room_id")
  private UUID roomId;

  @Column(name = "station_number")
  private Integer stationNumber;

  @Column(name = "map_x", precision = 5, scale = 2)
  private BigDecimal mapX;

  @Column(name = "map_y", precision = 5, scale = 2)
  private BigDecimal mapY;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp(source = SourceType.DB)
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Space() {}

  public Space(String name, SpaceType type, int capacity, String description) {
    setDetails(name, type, capacity, description);
  }

  void update(String name, SpaceType type, int capacity, String description) {
    if (roomId != null && type != SpaceType.WORKSTATION) {
      throw new ResourceConflictException(
          "Uma estação vinculada a uma sala não pode mudar de tipo.");
    }
    setDetails(name, type, capacity, description);
  }

  private void setDetails(String name, SpaceType type, int capacity, String description) {
    this.name = name == null ? null : name.strip();
    this.type = type;
    this.capacity = capacity;
    this.description = description == null ? null : description.strip();
  }

  void changeStatus(boolean active) {
    this.active = active;
  }

  void placeStation(UUID roomId, int number, BigDecimal x, BigDecimal y) {
    if (type != SpaceType.WORKSTATION || id.equals(roomId)) {
      throw new ResourceConflictException(
          "Somente estações individuais podem ser vinculadas a outra sala.");
    }
    this.roomId = roomId;
    this.stationNumber = number;
    this.mapX = x;
    this.mapY = y;
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

  public UUID getRoomId() {
    return roomId;
  }

  public Integer getStationNumber() {
    return stationNumber;
  }

  public BigDecimal getMapX() {
    return mapX;
  }

  public BigDecimal getMapY() {
    return mapY;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
