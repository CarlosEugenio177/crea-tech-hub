import { toBrazilianDate } from '../reservation/reservationDates.ts'

export function exclusiveDayEnd(date: string) {
  if (date === '9999-12-31') return undefined
  const next = new Date(`${date}T00:00:00Z`)
  next.setUTCDate(next.getUTCDate() + 1)
  return `${next.toISOString().slice(0, 10)}T00:00`
}

const dateTimeFormat = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

function splitLocalTimestamp(timestamp: string) {
  const [date, time = ''] = timestamp.split('T')
  return {
    date: toBrazilianDate(date),
    time: time.slice(0, 5),
  }
}

export function formatBlockInterval(startsAt: string, endsAt: string) {
  const start = splitLocalTimestamp(startsAt)
  const end = splitLocalTimestamp(endsAt)
  if (start.date === end.date) {
    return `${start.date}, das ${start.time} às ${end.time}`
  }
  return `${start.date} às ${start.time} até ${end.date} às ${end.time}`
}

export function formatInstant(isoInstant: string) {
  return dateTimeFormat.format(new Date(isoInstant))
}
