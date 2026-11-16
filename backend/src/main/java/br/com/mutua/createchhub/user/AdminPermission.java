package br.com.mutua.createchhub.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum AdminPermission {
  MANAGE_ACCESS("Gerenciar equipe e permissões"),
  MANAGE_SPACES("Gerenciar espaços");

  private final String description;

  AdminPermission(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
