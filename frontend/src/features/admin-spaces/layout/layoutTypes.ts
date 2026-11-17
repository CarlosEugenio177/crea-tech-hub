import type { components } from '../../../shared/api/schema'

export type Space = components['schemas']['Space']
export type BookingMode = components['schemas']['BookingMode']
export type PositionUpdate = components['schemas']['StationPositionUpdate']
export type Station = Space & {
  stationPlacement: NonNullable<Space['stationPlacement']>
}
export type Point = Pick<PositionUpdate, 'x' | 'y'>

export function isStation(space: Space): space is Station {
  return space.type === 'WORKSTATION' && space.stationPlacement !== undefined
}

export function boundedPoint(x: number, y: number): Point {
  const bound = (value: number) =>
    Math.round(Math.max(0, Math.min(100, value)) * 100) / 100
  return { x: bound(x), y: bound(y) }
}
