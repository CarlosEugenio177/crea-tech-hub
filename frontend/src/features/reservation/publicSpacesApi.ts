import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type PublicSpace = components['schemas']['PublicSpace']

const CATALOG_PATH = '/api/v1/spaces'
const CATALOG_SIZE = 100

export function usePublicSpaces() {
  return api.useInfiniteQuery(
    'get',
    CATALOG_PATH,
    { params: { query: { size: CATALOG_SIZE } } },
    {
      initialPageParam: 0,
      pageParamName: 'page',
      getNextPageParam: (lastPage) =>
        lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
      select: ({ pages }) => pages.flatMap((page) => page.items),
      retry: false,
    },
  )
}
