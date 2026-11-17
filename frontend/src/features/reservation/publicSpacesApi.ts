import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type PublicSpace = components['schemas']['PublicSpace']

const CATALOG_PATH = '/api/v1/spaces'
// O Hub tem poucos espaços: o máximo do contrato traz o catálogo inteiro em uma página.
const CATALOG_SIZE = 100

export function usePublicSpaces() {
  return api.useQuery(
    'get',
    CATALOG_PATH,
    { params: { query: { page: 0, size: CATALOG_SIZE } } },
    { retry: false },
  )
}
