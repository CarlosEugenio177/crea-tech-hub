export const ADMIN_TEAM = '/admin/equipe'
export const ADMIN_TEAM_NEW = '/admin/equipe/novo'
export const ADMIN_TEAM_AUDIT = '/admin/equipe/auditoria'
export const ADMIN_TEAM_MEMBER = '/admin/equipe/:id'

export function teamMemberPath(id: string) {
  return `${ADMIN_TEAM}/${id}`
}
