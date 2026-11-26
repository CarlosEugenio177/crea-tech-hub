import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import type { AvailableStation } from './availabilityApi.ts'
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
import { StationOptions } from './StationOptions.tsx'
import { StepPage } from './StepPage.tsx'

export function ChooseTimePage() {
  const navigate = useNavigate()
  const { draft, update } = useReservationDraft()
  const hours = usePublicOperatingHours()
  const [stationChoice, setStationChoice] = useState<{
    period: ReservationPeriod
    stations: AvailableStation[]
  }>()

  if (!draft.space) return <Navigate to={RESERVE} replace />

  function continueWith(period: ReservationPeriod, station?: AvailableStation) {
    update({ period, station })
    void navigate(RESERVE_REQUESTER)
  }

  return (
    <StepPage step={2} title="Escolha o horário" description={draft.space.name}>
      <div className={styles.schedule}>
        <div className={styles.booking}>
          <PeriodForm
            spaceId={draft.space.id}
            period={draft.period ?? draft.recommendation?.period}
            back={
              draft.recommendation
                ? { path: RESERVE_RECOMMENDATION, label: 'Voltar às sugestões' }
                : undefined
            }
            onSubmit={(period, stations) => {
              if (stations.length) setStationChoice({ period, stations })
              else continueWith(period)
            }}
            onEdit={() => setStationChoice(undefined)}
          />
          {stationChoice && (
            <StationOptions
              stations={stationChoice.stations}
              onChoose={(station) =>
                continueWith(stationChoice.period, station)
              }
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
