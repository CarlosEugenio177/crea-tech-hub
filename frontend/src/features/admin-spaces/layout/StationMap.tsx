import { useId, useRef, type KeyboardEvent } from 'react'
import type { Point, Station } from './layoutTypes.ts'
import { useStationDrag } from './useStationDrag.ts'
import styles from './RoomLayout.module.css'

type Props = {
  stations: Station[]
  selectedId?: string
  disabled: boolean
  point: (station: Station) => Point
  onMove: (station: Station, position: Point) => void
  onSelect: (id: string) => void
}

export function StationMap({
  stations,
  selectedId,
  disabled,
  point,
  onMove,
  onSelect,
}: Props) {
  const canvas = useRef<HTMLDivElement>(null)
  const descriptionId = useId()
  const drag = useStationDrag(canvas, point, onMove, onSelect)
  const onKeyDown = (
    event: KeyboardEvent<HTMLButtonElement>,
    station: Station,
  ) => {
    const delta = event.shiftKey ? 10 : 2
    const movements: Record<string, Point> = {
      ArrowLeft: { x: -delta, y: 0 },
      ArrowRight: { x: delta, y: 0 },
      ArrowUp: { x: 0, y: -delta },
      ArrowDown: { x: 0, y: delta },
    }
    const direction = movements[event.key]
    if (!direction || disabled) return
    event.preventDefault()
    const current = point(station)
    onSelect(station.id)
    onMove(station, { x: current.x + direction.x, y: current.y + direction.y })
  }

  return (
    <>
      <p id={descriptionId} className={styles.hint}>
        Arraste as estações. Pelo teclado, use as setas.
      </p>
      <div className={styles.room}>
        <div
          ref={canvas}
          className={styles.canvas}
          role="group"
          aria-label="Mapa de estações"
        >
          {stations.map((station) => {
            const position = point(station)
            return (
              <button
                key={station.id}
                type="button"
                disabled={disabled}
                className={`${styles.station} ${selectedId === station.id ? styles.selected : ''} ${!station.active ? styles.inactive : ''}`}
                style={{ left: `${position.x}%`, top: `${position.y}%` }}
                aria-label={`Mover estação ${station.stationPlacement.number}${station.active ? '' : ' (inativa)'}`}
                aria-pressed={selectedId === station.id}
                aria-describedby={descriptionId}
                onFocus={() => onSelect(station.id)}
                onClick={() => onSelect(station.id)}
                onKeyDown={(event) => onKeyDown(event, station)}
                onPointerDown={(event) => drag.start(event, station)}
                onPointerMove={(event) => drag.move(event, station)}
                onPointerUp={drag.stop}
                onPointerCancel={drag.stop}
                onLostPointerCapture={drag.stop}
              >
                <svg
                  viewBox="0 0 48 40"
                  aria-hidden="true"
                  className={styles.desk}
                >
                  <rect x="4" y="3" width="40" height="24" rx="5" />
                  <path d="M14 30v4a3 3 0 0 0 3 3h14a3 3 0 0 0 3-3v-4" />
                </svg>
                <span className={styles.stationNumber}>
                  {station.stationPlacement.number}
                </span>
              </button>
            )
          })}
        </div>
      </div>
    </>
  )
}
