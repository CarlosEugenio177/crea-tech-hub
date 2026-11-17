import { api } from '../../shared/api/client'
import type { components } from '../../shared/api/schema'

export type PublicSpace = components['schemas']['PublicSpace']

const PATH = '/api/v1/spaces'

export function usePublicSpaces() {
  return api.useInfiniteQuery(
    'get',
    PATH,
    { params: { query: { size: 100 } } },
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
