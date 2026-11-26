import type {
  AvailableStation,
  DayAvailability,
  FreeWindow,
} from './availabilityApi.ts'

const minutesOfDay = Array.from({ length: 24 * 60 }, (_, minute) => {
  const hour = String(Math.floor(minute / 60)).padStart(2, '0')
  return `${hour}:${String(minute % 60).padStart(2, '0')}`
})

export function bookingWindows(agenda?: DayAvailability) {
  if (!agenda || agenda.closed) return []
  return agenda.bookingMode === 'PER_STATION'
    ? agenda.stations.flatMap((station) => station.freeWindows)
    : agenda.freeWindows
}

export function startTimes(windows: readonly FreeWindow[]) {
  return minutesOfDay.filter((time) =>
    windows.some((window) => window.start <= time && time < window.end),
  )
}

export function endTimes(windows: readonly FreeWindow[], start: string) {
  if (!start) return []
  const latest = windows.reduce(
    (end, window) =>
      window.start <= start && start < window.end && window.end > end
        ? window.end
        : end,
    '',
  )
  return minutesOfDay.filter((time) => start < time && time <= latest)
}

export function stationsForPeriod(
  agenda: DayAvailability,
  start: string,
  end: string,
): AvailableStation[] {
  return agenda.stations.map(({ freeWindows, ...station }) => ({
    ...station,
    available: freeWindows.some(
      (window) => window.start <= start && end <= window.end,
    ),
  }))
}
