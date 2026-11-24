import { toBrazilianDate } from '../reservation/reservationDates.ts'

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
