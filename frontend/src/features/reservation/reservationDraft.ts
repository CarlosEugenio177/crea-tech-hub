import { useOutletContext } from 'react-router'
import type { Requester } from './requesterSchema.ts'

export type ReservationPeriod = { date: string; start: string; end: string }

export type ReservationDraft = {
  space?: { id: string; name: string }
  period?: ReservationPeriod
  requester?: Requester
}

export type ReservationFlowContext = {
  draft: ReservationDraft
  update: (changes: ReservationDraft) => void
}

export function useReservationDraft() {
  return useOutletContext<ReservationFlowContext>()
}
