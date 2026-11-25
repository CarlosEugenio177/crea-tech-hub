import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components, operations } from '../../shared/api/schema'

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
    '/api/v1/admin/reservations',
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
      queryKey: ['get', '/api/v1/admin/reservations'],
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
            queryKey: ['get', '/api/v1/spaces/{id}/availability'],
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
