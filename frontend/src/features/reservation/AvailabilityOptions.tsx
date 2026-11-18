import { useEffect, useId, useRef } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { useAvailability, type AvailableStation } from './availabilityApi.ts'
import type { ReservationPeriod } from './reservationDraft.ts'
import { PublicStationMap } from './PublicStationMap.tsx'

type Props = {
  spaceId: string
  period: ReservationPeriod
  onChoose: (station?: AvailableStation) => void
}

export function AvailabilityOptions({ spaceId, period, onChoose }: Props) {
  const result = useAvailability(spaceId, period)
  const heading = useRef<HTMLHeadingElement>(null)
  const titleId = useId()

  useEffect(() => {
    heading.current?.focus()
  }, [period])

  const firstFree = result.data?.stations.find((station) => station.available)

  return (
    <section aria-labelledby={titleId}>
      <h2 id={titleId} ref={heading} tabIndex={-1}>
        Disponibilidade
      </h2>
      {result.isError ? (
        <>
          <QueryStatus error={result.error} />
          <Button variant="secondary" onClick={() => void result.refetch()}>
            Tentar novamente
          </Button>
        </>
      ) : !result.data ? (
        <QueryStatus error={undefined} />
      ) : (
        <>
          {result.data.status === 'CLOSED' && (
            <Notice tone="info">
              O Hub está fechado nesse período. Escolha outro horário.
            </Notice>
          )}
          {result.data.status === 'UNAVAILABLE' && (
            <Notice tone="info">
              Não há vaga nesse período. Escolha outro horário.
            </Notice>
          )}
          {result.data.stations.length > 0 && (
            <PublicStationMap
              stations={result.data.stations}
              disabled={result.isFetching}
              onChoose={onChoose}
            />
          )}
          {firstFree && (
            <Button
              disabled={result.isFetching}
              onClick={() => onChoose(firstFree)}
            >
              Escolher uma estação livre pra mim
            </Button>
          )}
          {result.data.status === 'AVAILABLE' &&
            result.data.stations.length === 0 && (
              <>
                <p>Espaço livre nesse horário.</p>
                <Button disabled={result.isFetching} onClick={() => onChoose()}>
                  Continuar
                </Button>
              </>
            )}
        </>
      )}
    </section>
  )
}
