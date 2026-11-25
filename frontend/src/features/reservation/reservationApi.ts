import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'
import { cpfDigits } from './cpf.ts'
import type { Requester } from './requesterSchema.ts'
import type { ReservationPeriod } from './reservationDraft.ts'

export type ReservationReceipt = components['schemas']['ReservationReceipt']

export function useUsageRules() {
  return api.useQuery('get', '/api/v1/hub/usage-rules', {}, { retry: false })
}

export function useSubmitReservation(onSubmitted: (token: string) => void) {
  const mutation = api.useMutation('post', '/api/v1/reservations', {
    onSuccess: (data) => onSubmitted(data.trackingToken),
  })
  return {
    error: mutation.error,
    isPending: mutation.isPending,
    submit: (
      spaceId: string,
      stationId: string | undefined,
      period: ReservationPeriod,
      requester: Requester,
    ) => {
      const { rulesAccepted, cpf, whatsapp, ...details } = requester
      const body: components['schemas']['ReservationRequest'] = {
        spaceId,
        stationId,
        rulesAccepted,
        startsAt: `${period.date}T${period.start}`,
        endsAt: `${period.date}T${period.end}`,
        requester: {
          ...details,
          cpf: cpfDigits(cpf),
          whatsapp: whatsapp.replace(/\D/g, ''),
        },
      }
      mutation.mutate({ body })
    },
  }
}

export function useReservationReceipt(token: string) {
  return api.useQuery(
    'get',
    '/api/v1/reservations/receipt',
    {
      params: { header: { 'X-Reservation-Token': token } },
    },
    { retry: false, gcTime: 0, enabled: /^[A-Za-z0-9_-]{43}$/.test(token) },
  )
}

export function useCancelReservation(
  token: string,
  onCancelled: (receipt: ReservationReceipt) => void,
) {
  const queryClient = useQueryClient()
  const receiptKey = api.queryOptions('get', '/api/v1/reservations/receipt', {
    params: { header: { 'X-Reservation-Token': token } },
  }).queryKey

  const mutation = api.useMutation('post', '/api/v1/reservations/cancel', {
    onSuccess: async (data) => {
      queryClient.setQueryData(receiptKey, data)
      await queryClient.invalidateQueries({
        queryKey: ['get', '/api/v1/spaces/{id}/availability'],
      })
      onCancelled(data)
    },
    onError: async (error) => {
      if (error.status === 409) {
        await queryClient.invalidateQueries({
          queryKey: receiptKey,
        })
      }
    },
  })

  return {
    error: mutation.error,
    isPending: mutation.isPending,
    cancel: (confirmLateCancellation: boolean) => {
      const body: components['schemas']['ReservationCancellationRequest'] = {
        confirmLateCancellation,
      }
      mutation.mutate({
        params: { header: { 'X-Reservation-Token': token } },
        body,
      })
    },
  }
}
