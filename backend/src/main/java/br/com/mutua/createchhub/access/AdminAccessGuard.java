package br.com.mutua.createchhub.access;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessGuard {

  private final JdbcOperations jdbc;

  public AdminAccessGuard(JdbcOperations jdbc) {
    this.jdbc = jdbc;
  }

  public void lock() {
    jdbc.queryForObject("SELECT id FROM admin_access_guard WHERE id = 1 FOR UPDATE", Integer.class);
  }
}
