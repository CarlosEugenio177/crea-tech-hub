import { api } from '../../shared/api/client.ts'
import type { RecommendationCriteria } from './reservationDraft.ts'

export function useSpaceRecommendations({
  participants,
  period,
}: RecommendationCriteria) {
  return api.useQuery(
    'get',
    '/api/v1/spaces/recommendations',
    {
      params: {
        query: {
          participants,
          startsAt: `${period.date}T${period.start}`,
          endsAt: `${period.date}T${period.end}`,
        },
      },
    },
    { retry: false, gcTime: 0 },
  )
}
