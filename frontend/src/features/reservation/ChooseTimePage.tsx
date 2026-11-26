import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { AvailabilityOptions } from './AvailabilityOptions.tsx'
import styles from './ChooseTimePage.module.css'
import { formatOperatingHours } from './operatingHoursLabel.ts'
import { PeriodForm } from './PeriodForm.tsx'
import { usePublicOperatingHours } from './reservationApi.ts'
import {
  useReservationDraft,
  type ReservationPeriod,
} from './reservationDraft.ts'
import {
  RESERVE,
  RESERVE_RECOMMENDATION,
  RESERVE_REQUESTER,
} from './reservationRoutes.ts'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const hours = usePublicOperatingHours()
  const selectedPeriod = draft.period ?? draft.recommendation?.period
  const [period, setPeriod] = useState<ReservationPeriod | undefined>(
    draft.recommendation ? selectedPeriod : undefined,
  )
  const [lookup, setLookup] = useState(0)

  if (!draft.space) return <Navigate to={RESERVE} replace />

  return (
    <StepPage step={2} title="Escolha o horário" description={draft.space.name}>
      <div className={styles.schedule}>
        <div className={styles.booking}>
          <PeriodForm
            spaceId={draft.space.id}
            period={selectedPeriod}
            back={
              draft.recommendation
                ? { path: RESERVE_RECOMMENDATION, label: 'Voltar às sugestões' }
                : undefined
            }
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
        </div>
        {hours.data && (
          <section className={styles.hours}>
            <h2>Funcionamento do Hub</h2>
            <p>{formatOperatingHours(hours.data)}</p>
          </section>
        )}
      </div>
    </StepPage>
  )
}
