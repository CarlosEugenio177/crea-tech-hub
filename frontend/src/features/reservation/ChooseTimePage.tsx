import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { AvailabilityOptions } from './AvailabilityOptions.tsx'
import { PeriodForm } from './PeriodForm.tsx'
import {
  useReservationDraft,
  type ReservationPeriod,
} from './reservationDraft.ts'
import { RESERVE, RESERVE_REQUESTER } from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const [period, setPeriod] = useState<ReservationPeriod>()
  const [lookup, setLookup] = useState(0)

  if (!draft.space) return <Navigate to={RESERVE} replace />

  return (
    <StepPage
      step={2}
      title="Escolha uma data e um horário"
      description={`${draft.space.name}. Informe início e fim com precisão de minutos.`}
    >
      <PeriodForm
        period={draft.period}
        onSubmit={(nextPeriod) => {
          setPeriod(nextPeriod)
          setLookup((current) => current + 1)
        }}
        onEdit={() => setPeriod(undefined)}
      />
      {period && (
        <AvailabilityOptions
          key={lookup}
          spaceId={draft.space.id}
          period={period}
          onChoose={(station) => {
            update({ period, station })
            void navigate(RESERVE_REQUESTER)
          }}
        />
      )}
    </StepPage>
  )
}
