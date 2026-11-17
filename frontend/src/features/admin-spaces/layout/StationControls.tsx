import { Button } from '../../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../../shared/ui/ProblemNotice.tsx'
import { useChangeSpaceStatus } from '../spacesApi.ts'
import type { Point, Station } from './layoutTypes.ts'
import styles from './RoomLayout.module.css'

type Props = {
  station: Station
  point: Point
  disabled: boolean
  dirty: boolean
  onMove: (position: Point) => void
}

export function StationControls({
  station,
  point,
  disabled,
  dirty,
  onMove,
}: Props) {
  const status = useChangeSpaceStatus(station.id)
  return (
    <div className={styles.controls}>
      <div>
        <strong>{station.name}</strong>
        <p className={styles.hint}>{station.active ? 'Ativa' : 'Inativa'}</p>
      </div>
      <div
        className={styles.directionButtons}
        aria-label="Mover estação selecionada"
      >
        <Button
          variant="secondary"
          disabled={disabled}
          onClick={() => onMove({ x: point.x - 2, y: point.y })}
        >
          ← Esquerda
        </Button>
        <Button
          variant="secondary"
          disabled={disabled}
          onClick={() => onMove({ x: point.x + 2, y: point.y })}
        >
          Direita →
        </Button>
        <Button
          variant="secondary"
          disabled={disabled}
          onClick={() => onMove({ x: point.x, y: point.y - 2 })}
        >
          ↑ Acima
        </Button>
        <Button
          variant="secondary"
          disabled={disabled}
          onClick={() => onMove({ x: point.x, y: point.y + 2 })}
        >
          Abaixo ↓
        </Button>
      </div>
      <Button
        variant="secondary"
        disabled={disabled || dirty || status.isPending}
        onClick={() => status.change(!station.active)}
      >
        {station.active ? 'Desativar estação' : 'Ativar estação'}
      </Button>
      <ProblemNotice error={status.error} />
    </div>
  )
}
