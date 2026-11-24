import { api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'
import type { Requester } from './requesterSchema.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import { cpfDigits } from './cpf.ts'

export function useUsageRules() {
  return api.useQuery('get', '/api/v1/hub/usage-rules', {}, { retry: false })
}

export function usePublicOperatingHours() {
  return api.useQuery(
    'get',
    '/api/v1/hub/operating-hours',
    {},
    { retry: false },
  )
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
