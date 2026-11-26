import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type AvailableStation = components['schemas']['Station']
export type DayAvailability = components['schemas']['SpaceDayAgenda']
export type FreeWindow = components['schemas']['SpaceFreeWindow']

export const dayAgendaKey = [
  'get',
  '/api/v1/spaces/{id}/availability/day',
] as const

export function useDayAgenda(spaceId: string, date?: string) {
  return api.useQuery(
    ...dayAgendaKey,
    { params: { path: { id: spaceId }, query: { date: date ?? '' } } },
    { enabled: Boolean(date), retry: false, staleTime: 20_000 },
  )
}
