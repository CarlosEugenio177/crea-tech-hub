import styles from './CalendarStrip.module.css'
import { formatDate, shiftDate, today } from './reservationDates.ts'

const weekday = new Intl.DateTimeFormat('pt-BR', {
  weekday: 'short',
  timeZone: 'UTC',
})
const month = new Intl.DateTimeFormat('pt-BR', {
  month: 'long',
  year: 'numeric',
  timeZone: 'UTC',
})
const LAST_DATE = '9999-12-31'

type CalendarStripProps = {
  date: string
  onDateChange: (date: string) => void
}

export function CalendarStrip({ date, onDateChange }: CalendarStripProps) {
  const firstAllowed = today()
  const selected = new Date(`${date}T12:00:00Z`)
  const monday = shiftDate(date, -((selected.getUTCDay() + 6) % 7))
  const firstVisible = monday < firstAllowed ? firstAllowed : monday
  const days = Array.from({ length: 7 }, (_, offset) =>
    shiftDate(firstVisible, offset),
  ).filter((day) => /^\d{4}-/.test(day))

  return (
    <div className={styles.calendar} role="group" aria-label="Escolher dia">
      <div className={styles.header}>
        <strong>{month.format(selected)}</strong>
        <div className={styles.navigation}>
          <button
            type="button"
            aria-label="Dia anterior"
            disabled={date <= firstAllowed}
            onClick={() => onDateChange(shiftDate(date, -1))}
          >
            <span className={styles.previous} aria-hidden="true" />
          </button>
          <button
            type="button"
            aria-label="Dia seguinte"
            disabled={date === LAST_DATE}
            onClick={() => onDateChange(shiftDate(date, 1))}
          >
            <span className={styles.next} aria-hidden="true" />
          </button>
        </div>
      </div>
      <div className={styles.days}>
        {days.map((day) => (
          <button
            key={day}
            type="button"
            aria-label={formatDate(day)}
            aria-pressed={day === date}
            disabled={day < firstAllowed}
            onClick={() => onDateChange(day)}
          >
            <span>
              {weekday.format(new Date(`${day}T12:00:00Z`)).slice(0, 3)}
            </span>
            <strong>{day.slice(-2)}</strong>
          </button>
        ))}
      </div>
    </div>
  )
}
