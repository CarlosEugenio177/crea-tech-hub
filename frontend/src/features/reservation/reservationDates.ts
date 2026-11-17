const fullDate = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' })

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
