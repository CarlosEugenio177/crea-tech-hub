import { useEffect, useState } from 'react'
import type { components } from '../../../shared/api/schema'
import { arrangePoints, type Dimensions } from './layoutGeometry.ts'
import {
  boundedPoint,
  type Point,
  type PositionUpdate,
  type Space,
  type Station,
} from './layoutTypes.ts'

type Draft = {
  version: number
  dimensions: Dimensions
  positions: Record<string, PositionUpdate>
  removed: Record<
    string,
    components['schemas']['RoomLayoutRequest']['removedStations'][number]
  >
}

export function useRoomLayoutDraft(room: Space, stations: Station[]) {
  const [draft, setDraft] = useState<Draft | null>(null)
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const dimensions = draft?.dimensions ??
    room.roomDimensions ?? { width: 10, length: 6 }
  const visible = stations.filter((station) => !draft?.removed[station.id])
  const selected = visible.filter((station) => selectedIds.includes(station.id))
  const removedStations = Object.values(draft?.removed ?? {})
  const positions = Object.values(draft?.positions ?? {}).filter(
    (p) => !draft?.removed[p.id],
  )
  const dirty =
    positions.length > 0 ||
    removedStations.length > 0 ||
    dimensions.width !== (room.roomDimensions?.width ?? 10) ||
    dimensions.length !== (room.roomDimensions?.length ?? 6)

  useEffect(() => {
    if (!dirty) return
    const warn = (event: BeforeUnloadEvent) => event.preventDefault()
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  const start = (): Draft => ({
    version: room.version,
    dimensions,
    positions: {},
    removed: {},
  })
  const point = (station: Station): Point =>
    draft?.positions[station.id] ?? station.stationPlacement
  const moveMany = (updates: { station: Station; point: Point }[]) => {
    setDraft((previous) => {
      const next = previous ?? start()
      const changed = { ...next.positions }
      updates.forEach(({ station, point: position }) => {
        const bounded = boundedPoint(position.x, position.y)
        if (
          bounded.x === station.stationPlacement.x &&
          bounded.y === station.stationPlacement.y
        )
          delete changed[station.id]
        else
          changed[station.id] = {
            id: station.id,
            version: changed[station.id]?.version ?? station.version,
            ...bounded,
          }
      })
      return { ...next, positions: changed }
    })
  }
  const select = (id: string, additive = false) =>
    setSelectedIds((previous) =>
      additive
        ? previous.includes(id)
          ? previous.filter((item) => item !== id)
          : [...previous, id]
        : previous.includes(id)
          ? previous
          : [id],
    )
  const remove = (focusedId?: string) => {
    const removing =
      focusedId && !selectedIds.includes(focusedId)
        ? visible.filter((station) => station.id === focusedId)
        : selected
    setDraft((previous) => {
      const next = previous ?? start()
      const removed = { ...next.removed }
      removing.forEach((station) => {
        removed[station.id] = {
          id: station.id,
          version: next.positions[station.id]?.version ?? station.version,
        }
      })
      return { ...next, removed }
    })
    setSelectedIds([])
  }
  const arrange = (axis: 'x' | 'y', distribute: boolean) => {
    const result = arrangePoints(selected.map(point), axis, distribute)
    moveMany(
      selected.flatMap((station, i) =>
        result[i] ? [{ station, point: result[i] }] : [],
      ),
    )
  }
  return {
    dimensions,
    dirty,
    visible,
    selected,
    selectedIds,
    removedCount: removedStations.length,
    point,
    select,
    remove,
    arrange,
    selectAll: () => setSelectedIds(visible.map((station) => station.id)),
    clearSelection: () => setSelectedIds([]),
    move: (station: Station, point: Point) => moveMany([{ station, point }]),
    resize: (dimensions: Dimensions) =>
      setDraft((previous) => ({ ...(previous ?? start()), dimensions })),
    undoRemoval: () =>
      setDraft((previous) => previous && { ...previous, removed: {} }),
    reset: () => {
      setDraft(null)
      setSelectedIds([])
    },
    request: {
      version: draft?.version ?? room.version,
      ...dimensions,
      positions,
      removedStations,
    },
  }
}
