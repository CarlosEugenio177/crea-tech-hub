import { useId, useRef, useState, type KeyboardEvent } from 'react'
import { snapPosition, type Dimensions, type Guide } from './layoutGeometry.ts'
import type { Point, Station } from './layoutTypes.ts'
import { useStationDrag } from './useStationDrag.ts'
import styles from './RoomLayout.module.css'

type Props = {
  stations: Station[]
  selectedIds: string[]
  dimensions: Dimensions
  assisted: boolean
  grid: number
  disabled: boolean
  point: (station: Station) => Point
  onMove: (station: Station, position: Point) => void
  onSelect: (id: string, additive?: boolean) => void
  onRemove: (id?: string) => void
}

export function StationMap({
  stations,
  selectedIds,
  dimensions,
  assisted,
  grid,
  disabled,
  point,
  onMove,
  onSelect,
  onRemove,
}: Props) {
  const canvas = useRef<HTMLDivElement>(null)
  const descriptionId = useId()
  const [guides, setGuides] = useState<Guide[]>([])
  const drag = useStationDrag(
    canvas,
    point,
    onMove,
    onSelect,
    (position, bounds, station, free) => {
      if (!assisted || free) {
        setGuides([])
        return position
      }
      const result = snapPosition(
        position,
        stations.filter((other) => other.id !== station.id).map(point),
        bounds,
        dimensions,
        grid,
      )
      setGuides(result.guides)
      return result.point
    },
    () => setGuides([]),
  )
  const onKeyDown = (
    event: KeyboardEvent<HTMLButtonElement>,
    station: Station,
  ) => {
    if ((event.key === 'Delete' || event.key === 'Backspace') && !disabled) {
      event.preventDefault()
      onRemove(station.id)
      return
    }
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
        Arraste ou use as setas. Ctrl ou ⌘ seleciona várias; Alt suspende o
        encaixe.
      </p>
      <div className={styles.mapViewport}>
        <div
          className={styles.room}
          style={{
            minWidth: `${Math.max(320, dimensions.width * 44, (dimensions.width / dimensions.length) * 180)}px`,
          }}
        >
          <div
            ref={canvas}
            className={styles.canvas}
            style={{
              aspectRatio: `${dimensions.width} / ${dimensions.length}`,
              backgroundSize: `${(grid * 100) / dimensions.width}% ${(grid * 100) / dimensions.length}%`,
            }}
            role="group"
            aria-label="Mapa de estações"
          >
            {guides.map((guide) => (
              <div
                key={guide.axis}
                aria-hidden="true"
                className={
                  guide.axis === 'x'
                    ? styles.verticalGuide
                    : styles.horizontalGuide
                }
                style={
                  guide.axis === 'x'
                    ? { left: `${guide.value}%` }
                    : { top: `${guide.value}%` }
                }
              />
            ))}
            {stations.map((station) => {
              const position = point(station)
              return (
                <button
                  key={station.id}
                  type="button"
                  disabled={disabled}
                  className={`${styles.station} ${selectedIds.includes(station.id) ? styles.selected : ''} ${!station.active ? styles.inactive : ''}`}
                  style={{ left: `${position.x}%`, top: `${position.y}%` }}
                  aria-label={`Mover estação ${station.stationPlacement.number}${station.active ? '' : ' (inativa)'}`}
                  aria-pressed={selectedIds.includes(station.id)}
                  aria-describedby={descriptionId}
                  onClick={(event) =>
                    onSelect(station.id, event.ctrlKey || event.metaKey)
                  }
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
          <p className={styles.mapScale}>
            {dimensions.width.toLocaleString('pt-BR')} m ×{' '}
            {dimensions.length.toLocaleString('pt-BR')} m
          </p>
        </div>
      </div>
      {guides.some((guide) => guide.spacing !== undefined) && (
        <p className={styles.hint} role="status">
          Espaçamento igual entre os centros das estações.
        </p>
      )}
    </>
  )
}
