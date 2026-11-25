import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { Notice } from '../../shared/ui/Notice.tsx'
import { AvailabilityOptions } from './AvailabilityOptions.tsx'
import { formatOperatingHours } from './operatingHoursLabel.ts'
import { PeriodForm } from './PeriodForm.tsx'
import { usePublicOperatingHours } from './reservationApi.ts'
import {
  useReservationDraft,
  type ReservationPeriod,
} from './reservationDraft.ts'
import { RESERVE, RESERVE_REQUESTER } from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const hours = usePublicOperatingHours()
  const [period, setPeriod] = useState<ReservationPeriod>()
  const [lookup, setLookup] = useState(0)

  if (!draft.space) return <Navigate to={RESERVE} replace />

  return (
    <StepPage
      step={2}
      title="Escolha uma data e um horário"
      description={`${draft.space.name}. Informe início e fim com precisão de minutos.`}
    >
      {hours.data && (
        <Notice tone="info">{formatOperatingHours(hours.data)}</Notice>
      )}
      <PeriodForm
        spaceId={draft.space.id}
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
