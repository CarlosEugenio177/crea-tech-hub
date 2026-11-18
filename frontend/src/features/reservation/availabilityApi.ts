import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'
import type { ReservationPeriod } from './reservationDraft.ts'

export type AvailableStation = components['schemas']['Station']

export function useAvailability(spaceId: string, period: ReservationPeriod) {
  return api.useQuery(
    'get',
    '/api/v1/spaces/{id}/availability',
    {
      params: {
        path: { id: spaceId },
        query: {
          startsAt: `${period.date}T${period.start}`,
          endsAt: `${period.date}T${period.end}`,
        },
      },
    },
    { retry: false, gcTime: 0 },
  )
}
