import { useEffect } from 'react'
import { keepPreviousData, useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type SpaceBlock = components['schemas']['SpaceBlock']
export type SpaceBlockRequest = components['schemas']['SpaceBlockRequest']

const BLOCKS_PATH = '/api/v1/admin/space-blocks'
const CANCEL_PATH = '/api/v1/admin/space-blocks/{id}/cancel'
const SPACES_PATH = '/api/v1/admin/spaces'
const PAGE_SIZE = 20

export type SpaceBlocksFilter = {
  page: number
  spaceId?: string
  active?: boolean
  from?: string
  to?: string
}

export function useSpaceBlocks(filter: SpaceBlocksFilter) {
  return api.useQuery(
    'get',
    BLOCKS_PATH,
    {
      params: {
        query: {
          page: filter.page,
          size: PAGE_SIZE,
          spaceId: filter.spaceId,
          active: filter.active,
          from: filter.from,
          to: filter.to,
        },
      },
    },
    { placeholderData: keepPreviousData, retry: false },
  )
}

export function useAllAdminSpaces() {
  const query = api.useInfiniteQuery(
    'get',
    SPACES_PATH,
    { params: { query: { size: 100 } } },
    {
      initialPageParam: 0,
      pageParamName: 'page',
      retry: false,
      getNextPageParam: (page) =>
        page.page + 1 < page.totalPages ? page.page + 1 : undefined,
      select: ({ pages }) => pages.flatMap((page) => page.items),
    },
  )
  const { hasNextPage, isFetchingNextPage, isError, fetchNextPage } = query
  useEffect(() => {
    if (hasNextPage && !isFetchingNextPage && !isError) void fetchNextPage()
  }, [hasNextPage, isFetchingNextPage, isError, fetchNextPage])
  return query
}

export function useCreateSpaceBlock(onSaved: () => void) {
  const queryClient = useQueryClient()
  const create = api.useMutation('post', BLOCKS_PATH, {
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['get', BLOCKS_PATH] })
      onSaved()
    },
  })

  return {
    error: create.error,
    isPending: create.isPending,
    submit: (block: SpaceBlockRequest) => create.mutate({ body: block }),
  }
}

export function useCancelSpaceBlock(id: string) {
  const queryClient = useQueryClient()
  const mutation = api.useMutation('post', CANCEL_PATH, {
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['get', BLOCKS_PATH] })
    },
  })

  return {
    error: mutation.error,
    isPending: mutation.isPending,
    cancel: () => mutation.mutate({ params: { path: { id } } }),
  }
}
