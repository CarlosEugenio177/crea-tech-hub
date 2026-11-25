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
