import { useEffect } from 'react'
import { useQueryClient, type InfiniteData } from '@tanstack/react-query'
import { api } from '../../../shared/api/client.ts'
import type { components } from '../../../shared/api/schema'
import type { BookingMode, PositionUpdate, Space } from './layoutTypes.ts'

const ROOM_PATH = '/api/v1/admin/spaces/{id}'
const STATIONS_PATH = '/api/v1/admin/spaces/{roomId}/stations'
const BATCH_PATH = '/api/v1/admin/spaces/{roomId}/stations/batch'
const POSITIONS_PATH = '/api/v1/admin/spaces/{roomId}/station-positions'
type StationPage = components['schemas']['SpacePage']
type BatchRequest = components['schemas']['StationBatchRequest']

export function useRoomStations(roomId: string, enabled: boolean) {
  const query = api.useInfiniteQuery(
    'get',
    STATIONS_PATH,
    { params: { path: { roomId }, query: { size: 100 } } },
    {
      enabled,
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

export function useBookingMode(roomId: string) {
  const cache = useQueryClient()
  const mutation = api.useMutation(
    'put',
    '/api/v1/admin/spaces/{id}/booking-mode',
    {
      onSuccess: async (room) => {
        cache.setQueryData(
          api.queryOptions('get', ROOM_PATH, {
            params: { path: { id: roomId } },
          }).queryKey,
          room,
        )
        await cache.invalidateQueries({
          queryKey: ['get', '/api/v1/admin/spaces'],
        })
      },
    },
  )
  return {
    error: mutation.error,
    isPending: mutation.isPending,
    submit: (bookingMode: BookingMode) =>
      mutation.mutate({
        params: { path: { id: roomId } },
        body: { bookingMode },
      }),
  }
}

export function useGenerateStations(roomId: string) {
  const cache = useQueryClient()
  const mutation = api.useMutation('post', BATCH_PATH, {
    onSuccess: async () => {
      await Promise.all([
        cache.invalidateQueries({ queryKey: ['get', STATIONS_PATH] }),
        cache.invalidateQueries({ queryKey: ['get', '/api/v1/admin/spaces'] }),
      ])
    },
  })
  return {
    error: mutation.error,
    isPending: mutation.isPending,
    submit: (body: BatchRequest) =>
      mutation.mutate({ params: { path: { roomId } }, body }),
  }
}

export function useSavePositions(roomId: string, onSaved: () => void) {
  const cache = useQueryClient()
  const mutation = api.useMutation('put', POSITIONS_PATH, {
    onSuccess: (saved: Space[]) => {
      const byId = new Map(saved.map((station) => [station.id, station]))
      cache.setQueriesData<InfiniteData<StationPage>>(
        { queryKey: ['get', STATIONS_PATH] },
        (previous) =>
          previous && {
            ...previous,
            pages: previous.pages.map((page) => ({
              ...page,
              items: page.items.map(
                (station) => byId.get(station.id) ?? station,
              ),
            })),
          },
      )
      onSaved()
    },
  })
  return {
    error: mutation.error,
    isPending: mutation.isPending,
    submit: (positions: PositionUpdate[]) =>
      mutation.mutate({ params: { path: { roomId } }, body: { positions } }),
  }
}
