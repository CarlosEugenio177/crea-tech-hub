package br.com.mutua.createchhub.shared.error;

public class ResourceConflictException extends RuntimeException {

  public ResourceConflictException(String message) {
    super(message);
  }
}
