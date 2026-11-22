import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

export type OperatingHours = components['schemas']['OperatingHours']
export type OperatingHoursRequest =
  components['schemas']['OperatingHoursRequest']

const OPERATING_HOURS_PATH = '/api/v1/admin/hub/operating-hours'

export function useAdminOperatingHours() {
  return api.useQuery(
    'get',
    OPERATING_HOURS_PATH,
    {},
    { gcTime: 0, retry: false },
  )
}

export function useUpdateOperatingHours(onSaved: () => void) {
  const queryClient = useQueryClient()
  const update = api.useMutation('put', OPERATING_HOURS_PATH, {
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['get', OPERATING_HOURS_PATH],
      })
      onSaved()
    },
  })

  return {
    error: update.error,
    isPending: update.isPending,
    submit: (hours: OperatingHoursRequest) => update.mutate({ body: hours }),
  }
}
