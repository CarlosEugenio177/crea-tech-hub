import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import type { AvailableStation } from './availabilityApi.ts'
import styles from './PublicStationMap.module.css'

type Props = {
  stations: AvailableStation[]
  disabled: boolean
  onChoose: (station: AvailableStation) => void
}

export function PublicStationMap({ stations, disabled, onChoose }: Props) {
  const [selectedId, select] = useState('')
  const selected = stations.find(
    (station) => station.id === selectedId && station.available,
  )
  return (
    <div className={styles.options}>
      <div className={styles.viewport}>
        <div className={styles.room} role="group" aria-label="Mapa de estações">
          {stations.map((station) => (
            <button
              key={station.id}
              type="button"
              className={styles.station}
              style={{ left: `${station.x}%`, top: `${station.y}%` }}
              aria-label={`Estação ${station.number}, ${station.available ? 'livre' : 'indisponível'}`}
              aria-pressed={selected?.id === station.id}
              disabled={disabled || !station.available}
              onClick={() => select(station.id)}
            >
              <strong>{station.number}</strong>
              <span>{station.available ? 'Livre' : 'Indisp.'}</span>
            </button>
          ))}
        </div>
      </div>
      <SelectField
        label="Escolher estação pelo número"
        value={selected?.id ?? ''}
        disabled={disabled || !stations.some((station) => station.available)}
        onChange={(event) => select(event.target.value)}
      >
        <option value="">Selecione uma estação livre</option>
        {stations.map((station) => (
          <option
            key={station.id}
            value={station.id}
            disabled={!station.available}
          >
            Estação {station.number} ·{' '}
            {station.available ? 'Livre' : 'Indisponível'}
          </option>
        ))}
      </SelectField>
      <Button
        disabled={disabled || !selected}
        onClick={() => selected && onChoose(selected)}
      >
        Continuar com esta estação
      </Button>
    </div>
  )
}
