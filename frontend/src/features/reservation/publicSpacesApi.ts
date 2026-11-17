import { api } from '../../shared/api/client'
import type { components } from '../../shared/api/schema'

export type PublicSpace = components['schemas']['PublicSpace']

const PATH = '/api/v1/spaces'

export function usePublicSpaces() {
  return api.useQuery(
    'get',
    PATH,
    { params: { query: { page: 0, size: 100 } } },
    { retry: false },
  )
}
