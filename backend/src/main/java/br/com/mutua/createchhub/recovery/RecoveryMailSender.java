package br.com.mutua.createchhub.recovery;

public interface RecoveryMailSender {
  void requireConfigured();

  void send(String email, String token);
}
