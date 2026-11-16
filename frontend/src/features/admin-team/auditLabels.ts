import type { AuditEvent } from './teamApi.ts'

const ACTION_LABELS: Record<string, string> = {
  ADMIN_CREATED: 'Conta criada',
  PERMISSIONS_CHANGED: 'Permissões alteradas',
  STATUS_CHANGED: 'Situação alterada',
  PASSWORD_CHANGED: 'Senha alterada',
  PASSWORD_RESET: 'Senha redefinida',
}

const dateTime = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

export function auditActionLabel(event: AuditEvent) {
  return ACTION_LABELS[event.action] ?? event.action
}

export function auditDetail(event: AuditEvent) {
  if (event.action !== 'STATUS_CHANGED') return event.detail
  return event.detail.endsWith('true') ? 'Conta ativada.' : 'Conta desativada.'
}

export function auditDate(event: AuditEvent) {
  return dateTime.format(new Date(event.createdAt))
}
