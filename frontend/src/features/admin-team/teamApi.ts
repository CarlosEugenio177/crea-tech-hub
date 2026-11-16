import { keepPreviousData, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client'
import type { components } from '../../shared/api/schema'

export type TeamMember = components['schemas']['AdminTeamMember']
export type PermissionOption = components['schemas']['PermissionOption']
export type AuditEvent = components['schemas']['AccessAuditEvent']
type AdminPermission = components['schemas']['AdminPermission']
type NewMember = components['schemas']['AdminCreateRequest']

const TEAM_PATH = '/api/v1/admin/team'
const MEMBER_PATH = '/api/v1/admin/team/{id}'
const PERMISSIONS_PATH = '/api/v1/admin/team/{id}/permissions'
const STATUS_PATH = '/api/v1/admin/team/{id}/status'
const OPTIONS_PATH = '/api/v1/admin/team/permissions'
const AUDIT_PATH = '/api/v1/admin/team/audit'
const PAGE_SIZE = 20

function useRefreshTeam() {
  const queryClient = useQueryClient()
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: ['get', TEAM_PATH] }),
      queryClient.invalidateQueries({ queryKey: ['get', AUDIT_PATH] }),
    ])
}

export function useTeam(page: number, size = PAGE_SIZE) {
  return api.useQuery(
    'get',
    TEAM_PATH,
    { params: { query: { page, size } } },
    { placeholderData: keepPreviousData, retry: false },
  )
}

export function useTeamMember(id: string) {
  return api.useQuery(
    'get',
    MEMBER_PATH,
    { params: { path: { id } } },
    // Sem cache: o formulário de permissões sempre parte do cadastro atual.
    { gcTime: 0, retry: false },
  )
}

export function usePermissionOptions() {
  return api.useQuery('get', OPTIONS_PATH, {}, { retry: false })
}

export function useAudit(page: number) {
  return api.useQuery(
    'get',
    AUDIT_PATH,
    { params: { query: { page, size: PAGE_SIZE } } },
    { placeholderData: keepPreviousData, retry: false },
  )
}

export function useCreateMember(onSaved: () => void) {
  const refresh = useRefreshTeam()
  const create = api.useMutation('post', TEAM_PATH, {
    onSuccess: async () => {
      await refresh()
      onSaved()
    },
  })

  return {
    error: create.error,
    isPending: create.isPending,
    submit: (member: NewMember) => create.mutate({ body: member }),
  }
}

export function useChangePermissions(id: string, onSaved: () => void) {
  const refresh = useRefreshTeam()
  const change = api.useMutation('put', PERMISSIONS_PATH, {
    onSuccess: async () => {
      await refresh()
      onSaved()
    },
  })

  return {
    error: change.error,
    isPending: change.isPending,
    submit: (permissions: AdminPermission[]) =>
      change.mutate({ params: { path: { id } }, body: { permissions } }),
  }
}

export function useChangeMemberStatus(id: string) {
  const refresh = useRefreshTeam()
  const status = api.useMutation('patch', STATUS_PATH, { onSuccess: refresh })

  return {
    error: status.error,
    isPending: status.isPending,
    change: (active: boolean) =>
      status.mutate({ params: { path: { id } }, body: { active } }),
  }
}
