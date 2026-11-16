export const ADMIN_SPACES = '/admin/espacos'
export const ADMIN_SPACE_NEW = '/admin/espacos/novo'
export const ADMIN_SPACE_EDIT = '/admin/espacos/:id'

export function spaceEditPath(id: string) {
  return `${ADMIN_SPACES}/${id}`
}
