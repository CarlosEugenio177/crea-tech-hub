import { useState } from 'react'
import { Outlet } from 'react-router'
import type {
  ReservationDraft,
  ReservationFlowContext,
} from './reservationDraft.ts'

export function ReservationFlow() {
  const [draft, setDraft] = useState<ReservationDraft>({})
  const context: ReservationFlowContext = {
    draft,
    update: (changes) => setDraft((current) => ({ ...current, ...changes })),
  }

  return <Outlet context={context} />
}
