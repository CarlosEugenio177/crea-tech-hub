package br.com.mutua.createchhub.access;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccessAudit {

  private final AccessAuditRepository events;

  public AccessAudit(AccessAuditRepository events) {
    this.events = events;
  }

  public void record(UUID actor, UUID target, String action, String detail) {
    events.save(new AccessAuditEvent(actor, target, action, detail));
  }
}
