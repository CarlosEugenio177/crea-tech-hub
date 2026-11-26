import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components, operations } from '../../shared/api/schema'
import { dayAgendaKey } from './availabilityApi.ts'

const QUEUE_PATH = '/api/v1/admin/reservations'

type Query = NonNullable<
  operations['listAdminReservations']['parameters']['query']
>

export type ReservationFilters = Pick<
  Query,
  'search' | 'space' | 'date' | 'status'
>
export type AdminReservationEntry =
  components['schemas']['AdminReservationEntry']

type DecisionRequest = components['schemas']['AdminReservationDecisionRequest']
export type AdminReservationAction = DecisionRequest['action']

export const statusLabels: Record<
  components['schemas']['ReservationStatus'],
  string
> = {
  PENDING: 'Pendente de análise',
  CONFIRMED: 'Confirmada',
  REJECTED: 'Recusada',
  CANCELLED: 'Cancelada',
}

export function useAdminReservations(
  filters: ReservationFilters,
  page: number,
  enabled: boolean,
) {
  return api.useQuery(
    'get',
    QUEUE_PATH,
    {
      params: { query: { ...filters, page, size: 20 } },
    },
    { retry: false, gcTime: 0, enabled },
  )
}

export function useDecideAdminReservation(
  id: string,
  expectedStatus: DecisionRequest['expectedStatus'],
  onSaved: () => void,
) {
  const client = useQueryClient()
  const refreshQueue = () =>
    client.invalidateQueries({
      queryKey: ['get', QUEUE_PATH],
    })
  const decision = api.useMutation(
    'post',
    '/api/v1/admin/reservations/{id}/decisions',
    {
      onSuccess: async () => {
        onSaved()
        await Promise.all([
          refreshQueue(),
          client.invalidateQueries({
            queryKey: dayAgendaKey,
          }),
        ])
      },
      onError: async (error) => {
        if (error.status === 409) await refreshQueue()
      },
    },
  )

  return {
    error: decision.error,
    isPending: decision.isPending,
    submit: (action: AdminReservationAction, note: string) =>
      decision.mutate({
        params: { path: { id } },
        body: { expectedStatus, action, note: note.trim() },
      }),
  }
}

export function useRecordAdminAttendance(id: string) {
  const client = useQueryClient()
  const refreshQueue = () =>
    client.invalidateQueries({ queryKey: ['get', QUEUE_PATH] })
  const mutation = api.useMutation(
    'put',
    '/api/v1/admin/reservations/{id}/attendance',
    {
      onSuccess: refreshQueue,
      onError: async (error) => {
        if (error.status === 409) await refreshQueue()
      },
    },
  )

  return {
    error: mutation.error,
    isPending: mutation.isPending,
    record: (noShow: boolean) =>
      mutation.mutate({ params: { path: { id } }, body: { noShow } }),
  }
}

export function useAdminRequesterBlock(id: string, onSaved: () => void) {
  const client = useQueryClient()
  const refreshQueue = () =>
    client.invalidateQueries({ queryKey: ['get', QUEUE_PATH] })
  const onSuccess = async () => {
    onSaved()
    await refreshQueue()
  }
  const block = api.useMutation(
    'put',
    '/api/v1/admin/reservations/{id}/requester-block',
    {
      onSuccess,
      onError: async (error) => {
        if (error.status === 409) await refreshQueue()
      },
    },
  )
  const release = api.useMutation(
    'delete',
    '/api/v1/admin/reservations/{id}/requester-block',
    {
      onSuccess,
    },
  )
  const params = { path: { id } }

  return {
    error: block.error ?? release.error,
    isPending: block.isPending || release.isPending,
    submit: (durationDays: number) => {
      release.reset()
      block.mutate({ params, body: { durationDays } })
    },
    revoke: () => {
      block.reset()
      release.mutate({ params })
    },
  }
}
