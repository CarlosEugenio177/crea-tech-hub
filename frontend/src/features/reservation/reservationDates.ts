const hubDate = new Intl.DateTimeFormat('en-CA', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  timeZone: 'America/Fortaleza',
})
const fullDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' })
const hubDateTime = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: 'America/Fortaleza',
})

export const BRAZILIAN_DATE = /^\d{2}\/\d{2}\/\d{4}$/
export const TIME_24H = /^([01]\d|2[0-3]):[0-5]\d$/

export function today() {
  const parts = Object.fromEntries(
    hubDate.formatToParts().map(({ type, value }) => [type, value]),
  )
  return `${parts.year}-${parts.month}-${parts.day}`
}

export function shiftDate(date: string, days: number) {
  const time = new Date(`${date}T12:00:00Z`)
  time.setUTCDate(time.getUTCDate() + days)
  return time.toISOString().slice(0, 10)
}

export function formatDate(date: string) {
  return fullDate.format(new Date(`${date}T00:00`))
}

export function formatHubInstant(isoInstant: string) {
  return hubDateTime.format(new Date(isoInstant))
}

export function toIsoDate(brazilianDate: string) {
  const [day, month, year] = brazilianDate.split('/')
  return `${year}-${month}-${day}`
}

export function toBrazilianDate(isoDate: string) {
  const [year, month, day] = isoDate.split('-')
  return `${day}/${month}/${year}`
}

export function isCalendarDate(isoDate: string) {
  const [year, month, day] = isoDate.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  )
}
