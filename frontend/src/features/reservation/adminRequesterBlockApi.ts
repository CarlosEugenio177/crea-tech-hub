import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import { adminReservationQueueKey } from './adminReservationsApi.ts'

export function useAdminRequesterBlock(id: string, onSaved: () => void) {
  const client = useQueryClient()
  const refreshQueue = () =>
    client.invalidateQueries({ queryKey: adminReservationQueueKey })
  const onSuccess = async () => {
    onSaved()
    await refreshQueue()
  }
  const block = api.useMutation(
    'put',
    '/api/v1/admin/reservations/{id}/requester-block',
    {
      onSuccess,
      onError: async (error) => {
        if (error.status === 409) await refreshQueue()
      },
    },
  )
  const release = api.useMutation(
    'delete',
    '/api/v1/admin/reservations/{id}/requester-block',
    {
      onSuccess,
    },
  )
  const params = { path: { id } }

  return {
    error: block.error ?? release.error,
    isPending: block.isPending || release.isPending,
    submit: (durationDays: number) => {
      release.reset()
      block.mutate({ params, body: { durationDays } })
    },
    revoke: () => {
      block.reset()
      release.mutate({ params })
    },
  }
}
