import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components, operations } from '../../shared/api/schema'
import { dayAgendaKey } from './availabilityApi.ts'

const QUEUE_PATH = '/api/v1/admin/reservations'
export const adminReservationQueueKey = ['get', QUEUE_PATH] as const

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
  size = 20,
) {
  return api.useQuery(
    'get',
    QUEUE_PATH,
    {
      params: { query: { ...filters, page, size } },
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
      queryKey: adminReservationQueueKey,
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
    client.invalidateQueries({ queryKey: adminReservationQueueKey })
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
