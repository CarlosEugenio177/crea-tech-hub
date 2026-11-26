import { useEffect, useId, useRef } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import type { AvailableStation } from './availabilityApi.ts'
import { PublicStationMap } from './PublicStationMap.tsx'

type Props = {
  stations: AvailableStation[]
  onChoose: (station: AvailableStation) => void
}

export function StationOptions({ stations, onChoose }: Props) {
  const heading = useRef<HTMLHeadingElement>(null)
  const titleId = useId()

  useEffect(() => {
    heading.current?.focus()
  }, [])

  const firstFree = stations.find((station) => station.available)

  return (
    <section aria-labelledby={titleId}>
      <h2 id={titleId} ref={heading} tabIndex={-1}>
        Escolha uma estação
      </h2>
      <PublicStationMap stations={stations} onChoose={onChoose} />
      {firstFree && (
        <Button onClick={() => onChoose(firstFree)}>
          Escolher uma estação livre pra mim
        </Button>
      )}
    </section>
  )
}
