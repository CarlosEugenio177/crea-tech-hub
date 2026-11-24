import type { components } from '../../shared/api/schema'

type OperatingHours = components['schemas']['OperatingHours']
type DayOfWeek = OperatingHours['daysOfWeek'][number]

const ORDER: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
]

const SHORT_DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'segunda',
  TUESDAY: 'terça',
  WEDNESDAY: 'quarta',
  THURSDAY: 'quinta',
  FRIDAY: 'sexta',
  SATURDAY: 'sábado',
  SUNDAY: 'domingo',
}

function formatDays(days: DayOfWeek[]) {
  const sorted = [...days].sort((a, b) => ORDER.indexOf(a) - ORDER.indexOf(b))
  if (sorted.length === 0) return ''
  if (sorted.length === 1) return SHORT_DAY_LABELS[sorted[0]]

  const indices = sorted.map((day) => ORDER.indexOf(day))
  const isContiguous = indices.every(
    (index, position) => position === 0 || index === indices[position - 1] + 1,
  )
  if (isContiguous && sorted.length >= 3) {
    return `${SHORT_DAY_LABELS[sorted[0]]} a ${SHORT_DAY_LABELS[sorted[sorted.length - 1]]}`
  }

  const labels = sorted.map((day) => SHORT_DAY_LABELS[day])
  return `${labels.slice(0, -1).join(', ')} e ${labels[labels.length - 1]}`
}

export function formatOperatingHours(hours: OperatingHours) {
  const days = formatDays(hours.daysOfWeek)
  return `Funcionamento do Hub: ${days}, das ${hours.openingTime} às ${hours.closingTime}.`
}
