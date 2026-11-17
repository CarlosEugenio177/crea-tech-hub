import { useEffect, useState } from 'react'
import {
  boundedPoint,
  type Point,
  type PositionUpdate,
  type Station,
} from './layoutTypes.ts'

export function useStationPositions(stations: Station[]) {
  const [draft, setDraft] = useState<Record<string, PositionUpdate>>({})
  const [selectedId, select] = useState<string>()
  const changes = Object.values(draft)
  const selected = stations.find((station) => station.id === selectedId)

  useEffect(() => {
    if (changes.length === 0) return
    const warn = (event: BeforeUnloadEvent) => event.preventDefault()
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [changes.length])

  const point = (station: Station): Point =>
    draft[station.id] ?? station.stationPlacement
  const move = (station: Station, position: Point) => {
    const next = boundedPoint(position.x, position.y)
    setDraft((previous) => {
      if (
        next.x === station.stationPlacement.x &&
        next.y === station.stationPlacement.y
      ) {
        const copy = { ...previous }
        delete copy[station.id]
        return copy
      }
      return {
        ...previous,
        [station.id]: {
          id: station.id,
          version: previous[station.id]?.version ?? station.version,
          ...next,
        },
      }
    })
  }

  return { changes, selected, select, point, move, reset: () => setDraft({}) }
}
