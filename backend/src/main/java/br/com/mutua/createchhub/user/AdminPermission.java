package br.com.mutua.createchhub.user;

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
