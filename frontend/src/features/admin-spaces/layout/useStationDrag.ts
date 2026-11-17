import { useRef, type PointerEvent, type RefObject } from 'react'
import { boundedPoint, type Point, type Station } from './layoutTypes.ts'

type Drag = { id: string; pointerId: number; offsetX: number; offsetY: number }

export function useStationDrag(
  canvas: RefObject<HTMLDivElement | null>,
  point: (station: Station) => Point,
  move: (station: Station, position: Point) => void,
  select: (id: string) => void,
  adjust: (
    position: Point,
    bounds: DOMRect,
    station: Station,
    free: boolean,
  ) => Point,
  onStop: () => void,
) {
  const drag = useRef<Drag | null>(null)

  return {
    start(event: PointerEvent<HTMLButtonElement>, station: Station) {
      if (
        event.button !== 0 ||
        event.ctrlKey ||
        event.metaKey ||
        !canvas.current
      )
        return
      const bounds = canvas.current.getBoundingClientRect()
      if (bounds.width === 0 || bounds.height === 0) return
      const position = point(station)
      drag.current = {
        id: station.id,
        pointerId: event.pointerId,
        offsetX:
          event.clientX - bounds.left - (position.x * bounds.width) / 100,
        offsetY:
          event.clientY - bounds.top - (position.y * bounds.height) / 100,
      }
      event.preventDefault()
      event.currentTarget.focus({ preventScroll: true })
      event.currentTarget.setPointerCapture?.(event.pointerId)
      select(station.id)
    },
    move(event: PointerEvent<HTMLButtonElement>, station: Station) {
      const current = drag.current
      if (
        !current ||
        current.id !== station.id ||
        current.pointerId !== event.pointerId ||
        !canvas.current
      )
        return
      const bounds = canvas.current.getBoundingClientRect()
      if (!bounds.width || !bounds.height) return
      move(
        station,
        adjust(
          boundedPoint(
            ((event.clientX - bounds.left - current.offsetX) * 100) /
              bounds.width,
            ((event.clientY - bounds.top - current.offsetY) * 100) /
              bounds.height,
          ),
          bounds,
          station,
          event.altKey,
        ),
      )
    },
    stop(event: PointerEvent<HTMLButtonElement>) {
      if (drag.current?.pointerId !== event.pointerId) return
      drag.current = null
      onStop()
      if (event.currentTarget.hasPointerCapture?.(event.pointerId))
        event.currentTarget.releasePointerCapture(event.pointerId)
    },
  }
}
