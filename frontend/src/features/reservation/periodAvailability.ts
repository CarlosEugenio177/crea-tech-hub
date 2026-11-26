import type {
  AvailableStation,
  DayAvailability,
  FreeWindow,
} from './availabilityApi.ts'

type BookingDuration = DayAvailability['bookingDuration']

const minutesOfDay = Array.from({ length: 24 * 60 }, (_, minute) => {
  const hour = String(Math.floor(minute / 60)).padStart(2, '0')
  return `${hour}:${String(minute % 60).padStart(2, '0')}`
})

function toMinutes(time: string) {
  return Number(time.slice(0, 2)) * 60 + Number(time.slice(3))
}

export function bookingWindows(agenda?: DayAvailability) {
  if (!agenda || agenda.closed) return []
  return agenda.bookingMode === 'PER_STATION'
    ? agenda.stations.flatMap((station) => station.freeWindows)
    : agenda.freeWindows
}

export function startTimes(
  windows: readonly FreeWindow[],
  duration?: BookingDuration,
) {
  const minimum = duration?.minMinutes ?? 1
  const ranges = windows.map(({ start, end }) => ({
    first: toMinutes(start),
    last: toMinutes(end) - minimum,
  }))
  return minutesOfDay.filter((_, minute) =>
    ranges.some(({ first, last }) => minute >= first && minute <= last),
  )
}

export function endTimes(
  windows: readonly FreeWindow[],
  start: string,
  duration?: BookingDuration,
) {
  if (!start) return []
  const latest = windows.reduce(
    (end, window) =>
      window.start <= start && start < window.end && window.end > end
        ? window.end
        : end,
    '',
  )
  if (!latest) return []
  const startMinute = toMinutes(start)
  const minimum = duration?.minMinutes ?? 1
  const maximum = duration?.maxMinutes ?? minutesOfDay.length - 1
  const endMinute = Math.min(toMinutes(latest), startMinute + maximum)
  return minutesOfDay.slice(startMinute + minimum, endMinute + 1)
}

export function suggestPeriod(window: FreeWindow, duration?: BookingDuration) {
  const [end] = endTimes([window], window.start, duration).slice(-1)
  return end ? { start: window.start, end } : undefined
}

export function durationLabel(duration?: BookingDuration) {
  const { minMinutes, maxMinutes } = duration ?? {}
  if (minMinutes && maxMinutes)
    return minMinutes === maxMinutes
      ? `Duração de ${minMinutes} min por reserva.`
      : `Duração de ${minMinutes} a ${maxMinutes} min por reserva.`
  if (minMinutes) return `Duração mínima de ${minMinutes} min por reserva.`
  if (maxMinutes) return `Duração máxima de ${maxMinutes} min por reserva.`
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
