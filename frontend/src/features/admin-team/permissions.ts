import type { PermissionOption } from './teamApi.ts'

export function pickPermissions(
  options: PermissionOption[],
  selected: string[],
) {
  return options
    .map((option) => option.permission)
    .filter((permission) => selected.includes(permission))
}

export function permissionSummary(
  options: PermissionOption[],
  granted: string[],
) {
  if (granted.length === 0) return 'Sem permissões'
  return granted
    .map(
      (permission) =>
        options.find((option) => option.permission === permission)
          ?.description ?? permission,
    )
    .join(' · ')
}
