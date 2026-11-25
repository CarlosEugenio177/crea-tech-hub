const fullDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' })
const hubDateTime = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: 'America/Fortaleza',
})
const DAY_IN_MS = 24 * 60 * 60 * 1000

export const BRAZILIAN_DATE = /^\d{2}\/\d{2}\/\d{4}$/
export const TIME_24H = /^([01]\d|2[0-3]):[0-5]\d$/

export function today() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

export function formatDate(date: string) {
  return fullDate.format(new Date(`${date}T00:00`))
}

export function formatHubInstant(isoInstant: string) {
  return hubDateTime.format(new Date(isoInstant))
}

export function isLessThan24HoursBeforeStart(startsAt: string) {
  const startMs = new Date(`${startsAt.slice(0, 16)}:00-03:00`).getTime()
  return Date.now() > startMs - DAY_IN_MS
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
