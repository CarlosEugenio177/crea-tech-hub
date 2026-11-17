import { keepPreviousData, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type Space = components['schemas']['Space']
export type SpaceRequest = components['schemas']['SpaceRequest']

const LIST_PATH = '/api/v1/admin/spaces'
const SPACE_PATH = '/api/v1/admin/spaces/{id}'
const STATUS_PATH = '/api/v1/admin/spaces/{id}/status'
const PAGE_SIZE = 20

function useRefreshSpaces(onRefreshed?: () => void) {
  const queryClient = useQueryClient()
  return async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['get', LIST_PATH] }),
      queryClient.invalidateQueries({
        queryKey: ['get', '/api/v1/admin/spaces/{roomId}/stations'],
      }),
    ])
    onRefreshed?.()
  }
}

export function useSpaces(page: number, active?: boolean) {
  return api.useQuery(
    'get',
    LIST_PATH,
    { params: { query: { page, size: PAGE_SIZE, active } } },
    { placeholderData: keepPreviousData, retry: false },
  )
}

export function useSpace(id: string) {
  return api.useQuery(
    'get',
    SPACE_PATH,
    { params: { path: { id } } },
    // Sem cache: o formulário de edição sempre parte do cadastro atual.
    { gcTime: 0, retry: false },
  )
}

export function useCreateSpace(onSaved: () => void) {
  const refresh = useRefreshSpaces(onSaved)
  const create = api.useMutation('post', LIST_PATH, { onSuccess: refresh })

  return {
    error: create.error,
    isPending: create.isPending,
    submit: (space: SpaceRequest) => create.mutate({ body: space }),
  }
}

export function useUpdateSpace(id: string, onSaved: () => void) {
  const refresh = useRefreshSpaces(onSaved)
  const update = api.useMutation('put', SPACE_PATH, { onSuccess: refresh })

  return {
    error: update.error,
    isPending: update.isPending,
    submit: (space: SpaceRequest) =>
      update.mutate({ params: { path: { id } }, body: space }),
  }
}

export function useChangeSpaceStatus(id: string) {
  const refresh = useRefreshSpaces()
  const status = api.useMutation('patch', STATUS_PATH, { onSuccess: refresh })

  return {
    error: status.error,
    isPending: status.isPending,
    change: (active: boolean) =>
      status.mutate({ params: { path: { id } }, body: { active } }),
  }
}
